@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.ir

import me.tbsten.compose.preview.lab.compiler.PluginConfig
import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.compat.IrDeclarationOriginCompat
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irTemporary
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

/**
 * Builds the IR for the full preview list.
 *
 * - `listOf(CollectedPreview(...), ...)` / `emptyList()` construction
 * - `lazy { ... }` wrapping
 * - cross-module concatenation (`mutableListOf().apply { addAll(...) }`)
 */
internal class PreviewListIrBuilder(
    private val pluginContext: IrPluginContext,
    private val previews: List<PreviewFunctionInfo>,
    private val config: PluginConfig,
    private val compatContext: CompatContext,
) {
    private val previewBuilder = CollectedPreviewIrBuilder(pluginContext, compatContext)

    private val collectedPreviewType get() = previewBuilder.collectedPreviewType

    private val listOfCollectedPreviewType by lazy {
        pluginContext.referenceClass(
            ClassId(FqName("kotlin.collections"), Name.identifier("List")),
        )!!.typeWith(collectedPreviewType)
    }

    // ----- Preview list construction -----

    /** Builds either `listOf(CollectedPreview(...), ...)` or `emptyList()`. */
    fun buildPreviewsListExpr(builder: DeclarationIrBuilder, parent: IrDeclarationParent): IrExpression {
        if (previews.isEmpty()) return buildEmptyListCall(builder)

        val listOfFun = pluginContext.referenceFunctions(
            CallableId(FqName("kotlin.collections"), Name.identifier("listOf")),
        ).first { fn ->
            val valueParams = fn.owner.parameters.filter { it.kind == IrParameterKind.Regular }
            valueParams.size == 1 && valueParams[0].varargElementType != null
        }

        val elements = previews.map { previewBuilder.buildCollectedPreviewCall(it, builder, parent) }

        val vararg: IrVararg = IrVarargImpl(
            startOffset = SYNTHETIC_OFFSET,
            endOffset = SYNTHETIC_OFFSET,
            type = pluginContext.irBuiltIns.arrayClass.typeWith(collectedPreviewType),
            varargElementType = collectedPreviewType,
            elements = elements.toMutableList(),
        )

        return compatContext.irCall(builder, listOfFun, listOfCollectedPreviewType, listOf(collectedPreviewType)).apply {
            arguments[0] = vararg
        }
    }

    private fun buildEmptyListCall(builder: IrBuilderWithScope): IrExpression {
        val emptyListFun = pluginContext.referenceFunctions(
            CallableId(FqName("kotlin.collections"), Name.identifier("emptyList")),
        ).first()
        return compatContext.irCall(builder, emptyListFun, listOfCollectedPreviewType, listOf(collectedPreviewType))
    }

    // ----- PreviewExport wrapper -----

    private val previewExportClass by lazy {
        pluginContext.referenceClass(
            ClassId(FqName("me.tbsten.compose.preview.lab"), Name.identifier("PreviewExport")),
        ) ?: error("PreviewExport class not found on classpath")
    }

    private val previewExportType by lazy {
        previewExportClass.typeWith()
    }

    /**
     * Builds the IR for `PreviewExport(<lazyExpr>)` where [lazyExpr] is a `Lazy<List<CollectedPreview>>`
     * expression. The backing field of properties declared as
     * `val x by collectModulePreviews()` / `val x by collectAllModulePreviews()` ends up holding
     * the resulting `PreviewExport` instance, which acts as the marker type for cross-module
     * discovery in [GeneratePreviewExportHint].
     */
    fun buildPreviewExportCall(builder: DeclarationIrBuilder, lazyExpr: IrExpression): IrExpression {
        val ctor = previewExportClass.constructors.first()
        return IrConstructorCallImpl(
            startOffset = SYNTHETIC_OFFSET,
            endOffset = SYNTHETIC_OFFSET,
            type = previewExportType,
            symbol = ctor,
            typeArgumentsCount = 0,
            constructorTypeArgumentsCount = 0,
        ).apply {
            arguments[0] = lazyExpr
        }
    }

    // ----- Lazy wrapper -----

    /** Builds the IR for `lazy { valueExpr }`. */
    fun buildLazyCall(builder: DeclarationIrBuilder, valueExpr: IrExpression, parent: IrDeclarationParent): IrExpression {
        val lazyFun = pluginContext.referenceFunctions(
            CallableId(FqName("kotlin"), Name.identifier("lazy")),
        ).first { fn ->
            fn.owner.parameters.filter { it.kind == IrParameterKind.Regular }.size == 1
        }

        val lambdaFun = pluginContext.irFactory.buildFun {
            startOffset = SYNTHETIC_OFFSET
            endOffset = SYNTHETIC_OFFSET
            name = SpecialNames.ANONYMOUS
            returnType = listOfCollectedPreviewType
            origin = IrDeclarationOriginCompat.LOCAL_FUNCTION_FOR_LAMBDA
            visibility = DescriptorVisibilities.LOCAL
        }.also { lambda ->
            lambda.parent = parent
            lambda.body = DeclarationIrBuilder(pluginContext, lambda.symbol).irBlockBody {
                +irReturn(valueExpr)
            }
        }

        val lambdaType = pluginContext.irBuiltIns.functionN(0).typeWith(listOfCollectedPreviewType)

        val lambdaExpr = IrFunctionExpressionImpl(
            startOffset = SYNTHETIC_OFFSET,
            endOffset = SYNTHETIC_OFFSET,
            type = lambdaType,
            origin = IrStatementOrigin.LAMBDA,
            function = lambdaFun,
        )

        return compatContext.irCall(
            builder,
            lazyFun,
            pluginContext.referenceClass(
                ClassId(FqName("kotlin"), Name.identifier("Lazy")),
            )!!.typeWith(listOfCollectedPreviewType),
            listOf(listOfCollectedPreviewType),
        ).apply {
            arguments[0] = lambdaExpr
        }
    }

    // ----- Cross-module concatenation -----

    /**
     * Lazily-cached lookup of `me.tbsten.compose.preview.lab.distinctPreviewsById`.
     *
     * `referenceFunctions(CallableId)` walks every classpath entry, so caching avoids redoing
     * the scan when [buildConcatenatedPreviewsExpr] is invoked for multiple
     * `collectAllModulePreviews()` properties in the same compilation.
     */
    private val distinctPreviewsByIdFun by lazy {
        pluginContext.referenceFunctions(
            CallableId(FqName("me.tbsten.compose.preview.lab"), Name.identifier("distinctPreviewsById")),
        ).firstOrNull() ?: error(
            "me.tbsten.compose.preview.lab.distinctPreviewsById not found on the compilation classpath. " +
                "This usually means the compose-preview-lab runtime/core dependency is missing or there is " +
                "a core/plugin version mismatch.",
        )
    }

    /**
     * Lazily-cached per-declaration hint functions discovered via Metro 風 mechanism.
     *
     * Each entry is an [IrSimpleFunction] that, when called with no arguments, returns a single
     * `CollectedPreview` (1 hint = 1 `@Preview`)。 [buildConcatenatedPreviewsExpr] では
     * `add(hint())` 形で list に積む。
     *
     * Caching ensures the (potentially expensive) package walk in [discoverHintsV2] runs at
     * most once per [PreviewListIrBuilder] instance.
     */
    private val cachedHintsV2: List<IrSimpleFunction> by lazy { discoverHintsV2(pluginContext, compatContext) }

    /**
     * Builds an expression that concatenates this module's previews with previews from
     * dependency modules and removes id-duplicates.
     *
     * Generates (semantically equivalent):
     * ```kotlin
     * distinctPreviewsById(
     *     mutableListOf<CollectedPreview>().apply {
     *         addAll(thisModulePreviews)
     *         addAll(dep1Property)
     *         addAll(dep2Property)
     *         // ...
     *     }
     * )
     * ```
     *
     * `distinctPreviewsById` is needed because a dependency that itself uses
     * `collectAllModulePreviews()` re-exports its transitive previews. Without dedup, an
     * `app(all) → ui(all) → core(single)` chain would yield each `core` preview twice (once
     * via `core` hint, once via `ui` hint).
     */
    fun buildConcatenatedPreviewsExpr(builder: DeclarationIrBuilder, thisModulePreviews: IrExpression): IrExpression {
        val hints = cachedHintsV2
        val distinctFun = distinctPreviewsByIdFun

        if (hints.isEmpty()) {
            return compatContext.irCall(builder, distinctFun, listOfCollectedPreviewType).apply {
                arguments[0] = thisModulePreviews
            }
        }

        val mutableListOfFun = pluginContext.referenceFunctions(
            CallableId(FqName("kotlin.collections"), Name.identifier("mutableListOf")),
        ).first { fn ->
            fn.owner.parameters.filter { it.kind == IrParameterKind.Regular }.isEmpty()
        }

        val addAllFun = pluginContext.referenceFunctions(
            CallableId(FqName("kotlin.collections"), Name.identifier("addAll")),
        ).first { fn ->
            val params = fn.owner.parameters.filter { it.kind == IrParameterKind.Regular }
            params.size == 1 && params[0].varargElementType == null
        }
        val addFun = pluginContext.referenceFunctions(
            CallableId(
                ClassId(FqName("kotlin.collections"), Name.identifier("MutableCollection")),
                Name.identifier("add"),
            ),
        ).first { fn ->
            val params = fn.owner.parameters.filter { it.kind == IrParameterKind.Regular }
            params.size == 1
        }

        val mutableListType = pluginContext.referenceClass(
            ClassId(FqName("kotlin.collections"), Name.identifier("MutableList")),
        )!!.typeWith(collectedPreviewType)

        val concatenatedExpr = builder.irBlock {
            val listVar = irTemporary(
                compatContext.irCall(this, mutableListOfFun, mutableListType, listOf(collectedPreviewType)),
            )
            +compatContext.irCall(this, addAllFun, pluginContext.irBuiltIns.booleanType, listOf(collectedPreviewType)).apply {
                arguments[0] = compatContext.irGet(this@irBlock, listVar)
                arguments[1] = thisModulePreviews
            }
            // Per-declaration hint: 各 hint は CollectedPreview を 1 個 return するので
            // `add(hint(null))` で list に積む。 hint 関数は
            // `previewHint(value: PreviewHintMarker_<hash>?): CollectedPreview` で、 marker
            // param は IdSignature 区別だけが目的なので `null` を渡せば良い。
            for (hintFn in hints) {
                val markerParam = hintFn.parameters.firstOrNull { it.kind == IrParameterKind.Regular }
                val hintCall = compatContext.irCall(this, hintFn.symbol, collectedPreviewType).apply {
                    if (markerParam != null) {
                        arguments[0] = IrConstImpl.constNull(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, markerParam.type)
                    }
                }
                +compatContext.irCall(this, addFun, pluginContext.irBuiltIns.booleanType).apply {
                    arguments[0] = compatContext.irGet(this@irBlock, listVar)
                    arguments[1] = hintCall
                }
            }
            +compatContext.irGet(this, listVar)
        }

        return compatContext.irCall(builder, distinctFun, listOfCollectedPreviewType).apply {
            arguments[0] = concatenatedExpr
        }
    }
}
