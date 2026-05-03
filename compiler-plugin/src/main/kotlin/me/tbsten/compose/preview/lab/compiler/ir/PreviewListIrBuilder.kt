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
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.platform.jvm.isJvm

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
     * Lazily-cached dependency-module preview getters discovered via Metro-style hints.
     *
     * Each entry is an [IrSimpleFunction] that, when called, returns
     * `List<CollectedPreview>` for one upstream module. Two flavors live in this list:
     *
     * - **Manual `collectModulePreviews()` / `collectAllModulePreviews()`**: the [IrSimpleFunction]
     *   is the property's getter (resolved from the hint's `@PreviewExportHint(fqn = ...)` via
     *   [IrPluginContext.referenceProperties]).
     * - **Auto-hint** (no sentinel call written by the user): the [IrSimpleFunction] is a
     *   stand-alone provider function emitted by `GenerateAutoPreviewExport`, resolved via
     *   [IrPluginContext.referenceFunctions] when property resolution comes back empty.
     *
     * Caching ensures the (potentially expensive) hint-function classpath walk in
     * [collectDependencyGetters] runs at most once per [PreviewListIrBuilder] instance, even
     * when a module declares multiple `collectAllModulePreviews()` properties.
     */
    private val cachedDependencyGetters: List<IrSimpleFunction> by lazy { collectDependencyGetters() }

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
        val dependencyGetters = cachedDependencyGetters
        val distinctFun = distinctPreviewsByIdFun

        if (dependencyGetters.isEmpty()) {
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
            for (dependencyGetter in dependencyGetters) {
                val dependencyValue = compatContext.irCall(this, dependencyGetter.symbol, listOfCollectedPreviewType)
                +compatContext.irCall(
                    this,
                    addAllFun,
                    pluginContext.irBuiltIns.booleanType,
                    listOf(collectedPreviewType),
                ).apply {
                    arguments[0] = compatContext.irGet(this@irBlock, listVar)
                    arguments[1] = dependencyValue
                }
            }
            +compatContext.irGet(this, listVar)
        }

        return compatContext.irCall(builder, distinctFun, listOfCollectedPreviewType).apply {
            arguments[0] = concatenatedExpr
        }
    }

    /**
     * Discovers dependency-module preview getters via Metro-style hint functions.
     *
     * **Currently JVM-only.** On KLIB-based platforms (JS / Wasm / iOS) the hint generator skips
     * emission entirely (see `PreviewLabIrBodyFiller`) and this method returns an empty list.
     * The reason is a KLIB-specific signature clash: `referenceFunctions(CallableId)` requires
     * a fixed function name to enumerate overloads, but KLIB IdSignatures are derived from
     * `(name, parameter types)` only — they do not include parameter names or annotations — so
     * two hints generated from different modules with the same parameter type `PreviewExport`
     * collide at link time. Solving this would require either descriptor-based package scanning
     * (rejected at runtime in K2 IR) or generating a unique synthetic class per FQN to differentiate
     * parameter types (FIR-level work, out of scope for this PR).
     *
     * On JVM the file-facade class disambiguates hints with identical signatures, so a fixed-name
     * `referenceFunctions(...)` lookup returns every overload across the classpath as expected.
     *
     * Hints emitted by the current module (because `PreviewLabIrBodyFiller` and
     * `GenerateAutoPreviewExport` run hint generation in the same IR pass) are filtered out so
     * the aggregator does not double-count this module's own previews.
     *
     * Each hint's `@PreviewExportHint(fqn = ...)` is resolved in two stages so both manual
     * `collectModulePreviews()` properties and auto-generated provider functions can share the
     * same hint discovery path:
     *
     * 1. [IrPluginContext.referenceProperties] — finds manual `val x by collectModulePreviews()`
     *    properties, returns `property.getter`.
     * 2. [IrPluginContext.referenceFunctions] (fallback) — finds auto-generated
     *    `previewLabAutoProvider_*` functions emitted by `GenerateAutoPreviewExport` for
     *    modules that don't write any sentinel call.
     */
    private fun collectDependencyGetters(): List<IrSimpleFunction> {
        // KLIB cross-module aggregation requires the FIR-side hint generator (Kotlin 2.3.21+).
        // On older Kotlin we fall back to JVM-only legacy hints, where file-facade classes
        // disambiguate `previewLabExport(PreviewExport)` overloads at the bytecode level.
        if (!compatContext.supportsKlibCrossModuleHint() && pluginContext.platform?.isJvm() != true) {
            return emptyList()
        }

        val hintSymbols = pluginContext.referenceFunctions(
            GeneratePreviewExportHint.HINT_FUNCTION_CALLABLE_ID,
        )

        return hintSymbols.mapNotNull { hintSymbol ->
            val hintFunction = hintSymbol.owner

            // Skip hints generated by the current compilation; their preview lists are already
            // covered by `thisModulePreviews`. External declarations come back with the
            // `IR_EXTERNAL_DECLARATION_STUB` origin marker.
            if (hintFunction.origin != IrDeclarationOriginCompat.IR_EXTERNAL_DECLARATION_STUB) return@mapNotNull null

            // Validate parameter shape: a single Regular parameter whose type is either:
            // - the legacy `PreviewExport` (Kotlin <2.3.21 JVM hints)
            // - any per-module marker class in the hint package (Kotlin 2.3.21+ FIR hints)
            val regularParams = hintFunction.parameters.filter { it.kind == IrParameterKind.Regular }
            if (regularParams.size != 1) return@mapNotNull null
            val paramFqn = regularParams[0].type.classFqName
            val isLegacyShape = paramFqn == GeneratePreviewExportHint.PREVIEW_EXPORT_FQN
            // FIR-emitted marker classes live directly in the hint package, named
            // `PreviewLabExportMarker_<hash>`. The hash matches the auto-provider function
            // suffix, so once we know the marker class id we can derive the provider FQN
            // without needing the `@PreviewExportHint` annotation — which is critical because
            // IR-attached annotations on FIR-generated functions don't always survive into
            // the consumer's `kotlin.Metadata`.
            val isFirMarkerShape = paramFqn?.parent() == GeneratePreviewExportHint.HINT_PACKAGE &&
                paramFqn.shortName().asString().startsWith(MarkerClassPrefix)
            if (!isLegacyShape && !isFirMarkerShape) return@mapNotNull null

            val fqn: String = if (isFirMarkerShape) {
                val hash = paramFqn!!.shortName().asString().removePrefix(MarkerClassPrefix)
                "${GeneratePreviewExportHint.HINT_PACKAGE.asString()}.previewLabAutoProvider_$hash"
            } else {
                // Legacy `previewLabExport(PreviewExport)` hint: target FQN comes from the
                // `@PreviewExportHint(fqn = ...)` annotation that `GeneratePreviewExportHint`
                // attaches at IR construction time (kotlin.Metadata captures it because the
                // function itself is IR-generated).
                val hintAnnotation = hintFunction.annotations.firstOrNull { ann ->
                    ann.type.classFqName == GeneratePreviewExportHint.PREVIEW_EXPORT_HINT_FQN
                } ?: return@mapNotNull null
                (hintAnnotation.arguments.firstOrNull() as? IrConst)?.value as? String ?: return@mapNotNull null
            }

            // Default-package targets (no dots in FQN) resolve to FqName.ROOT.
            val lastDot = fqn.lastIndexOf('.')
            val packageFqName = if (lastDot < 0) FqName.ROOT else FqName(fqn.substring(0, lastDot))
            val callableName = Name.identifier(if (lastDot < 0) fqn else fqn.substring(lastDot + 1))
            val callableId = CallableId(packageFqName, callableName)

            // Manual `collectModulePreviews()` property → return its getter.
            val property = pluginContext.referenceProperties(callableId).firstOrNull()?.owner
            val propertyGetter = property?.getter
            if (propertyGetter != null) return@mapNotNull propertyGetter

            // Auto-generated provider function (no source-level property) → return the function itself.
            pluginContext.referenceFunctions(callableId).firstOrNull()?.owner
        }
    }

    private companion object {
        // Mirrors `PreviewLabHintFirGenerator.MarkerClassPrefix`. We deliberately keep the
        // constant duplicated here rather than reaching into the FIR module — `PreviewListIrBuilder`
        // is in the IR layer and consuming a FIR-only constant would create a one-way coupling
        // we don't have anywhere else.
        const val MarkerClassPrefix = "PreviewLabExportMarker_"
    }
}
