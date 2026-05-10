@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.ir

import me.tbsten.compose.preview.lab.compiler.PluginConfig
import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.compat.IrDeclarationOriginCompat
import me.tbsten.compose.preview.lab.compiler.error.PreviewExportNotFoundError
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.PreviewFunctionInfo
import me.tbsten.compose.preview.lab.compiler.error.RuntimeFunctionNotFoundError
import me.tbsten.compose.preview.lab.compiler.error.StdlibClassNotFoundError
import me.tbsten.compose.preview.lab.compiler.error.orThrow
import me.tbsten.compose.preview.lab.compiler.utils.callableIdOf
import me.tbsten.compose.preview.lab.compiler.utils.classIdOf
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

/**
 * Builds the IR that backs `val x by collectModulePreviews()` and
 * `val x by collectAllModulePreviews()`. The output is always a
 * `PreviewExport(lazy { Sequence<CollectedPreview> })`; the difference between the two
 * call sites lives in *how* the inner sequence is composed (this-module-only vs.
 * concatenated across the classpath, with `distinctPreviewsByIdSequence` dedup).
 *
 * **Sample call (single module, two `@Preview` declarations)**:
 *
 * ```kotlin
 * val previews = listOf(PreviewFunctionInfo("My1", ...), PreviewFunctionInfo("My2", ...))
 * val builder = PreviewListIrBuilder(pluginContext, previews, config, compat)
 * val sequenceExpr = builder.buildPreviewsSequenceExpr(decl, parent, scope = "default")
 * val lazyExpr = builder.buildLazyCall(decl, sequenceExpr, parent)
 * val ctorCall = builder.buildPreviewExportCall(decl, lazyExpr)
 * ```
 *
 * **Result IR is equivalent to**:
 *
 * ```kotlin
 * PreviewExport(lazy {
 *     lazyPreviewSequence(
 *         { CollectedPreview("My1", "My1", ..., content = { My1() }) },
 *         { CollectedPreview("My2", "My2", ..., content = { My2() }) },
 *     )
 * })
 * ```
 *
 * Each `CollectedPreview(...)` constructor call is wrapped in a `() -> CollectedPreview`
 * factory lambda so that drained-only-as-needed iteration (`asSequence().take(2)`) avoids
 * allocating the `@Composable` lambda for previews the consumer never reads.
 */
internal class PreviewListIrBuilder(
    private val pluginContext: IrPluginContext,
    private val previews: List<PreviewFunctionInfo>,
    private val config: PluginConfig,
    private val compatContext: CompatContext,
) {
    private val previewBuilder = CollectedPreviewIrBuilder(pluginContext, compatContext)

    private val collectedPreviewType get() = previewBuilder.collectedPreviewType

    private val sequenceOfCollectedPreviewType by lazy {
        pluginContext.referenceClass(
            classIdOf("kotlin.sequences", "Sequence"),
        ).orThrow { StdlibClassNotFoundError("kotlin.sequences.Sequence") }
            .typeWith(collectedPreviewType)
    }

    /** `() -> CollectedPreview` factory-lambda type used for the `lazyPreviewSequence` vararg. */
    private val factoryLambdaType by lazy {
        pluginContext.irBuiltIns.functionN(0).typeWith(collectedPreviewType)
    }

    private val lazyPreviewSequenceFun by lazy {
        val callableId = callableIdOf("me.tbsten.compose.preview.lab", "lazyPreviewSequence")
        pluginContext.referenceFunctions(callableId).firstOrNull()
            .orThrow { RuntimeFunctionNotFoundError(callableId) }
    }

    // ----- Preview list construction -----

    /**
     * Builds `lazyPreviewSequence({ CollectedPreview(...) }, { CollectedPreview(...) }, ...)`
     * for the previews that participate in [scope]. A preview enters the result whenever
     * [scope] appears anywhere in its `@ComposePreviewLabOption(collectScopes = [...])`
     * array, so the same preview can show up in multiple `collectModulePreviews(scope = ...)`
     * call sites if it lists multiple scopes.
     *
     * **Empty-case sample**: with no preview matching the scope, emits
     * `lazyPreviewSequence()` (no factories), which returns an empty sequence at runtime.
     */
    fun buildPreviewsSequenceExpr(builder: DeclarationIrBuilder, parent: IrDeclarationParent, scope: String): IrExpression {
        val scopedPreviews = previews.filter { scope in it.scopes }
        val factories = scopedPreviews.map { previewInfo ->
            buildPreviewFactoryLambda(builder, parent) { factoryFun ->
                previewBuilder.buildCollectedPreviewCall(previewInfo, builder, factoryFun)
            }
        }
        return buildLazyPreviewSequenceCall(builder, factories)
    }

    /**
     * Per-builder counter used to give every synthesized factory lambda a distinct name.
     *
     * Sibling anonymous lambdas with `SpecialNames.ANONYMOUS` and identical body shape
     * end up colliding under the JVM lowering's `<containing>$N` mangling — the
     * "Platform declaration clash: `_get_previews_$0()`" error from kctfork. Tagging
     * each factory with `previewFactory_$N` keeps the IR-level name unique without
     * leaking into the public byte-code surface (the lambdas remain `LOCAL`).
     */
    private var factoryLambdaCounter: Int = 0

    /**
     * Wraps a single `CollectedPreview(...)` constructor call (built by [bodyBuilder]) in a
     * `() -> CollectedPreview` factory lambda. The lambda body returns `bodyBuilder()`; the
     * `Composable` content lambda inside `CollectedPreview` is therefore not allocated until
     * the sequence iteration reaches this element.
     */
    private fun buildPreviewFactoryLambda(
        builder: DeclarationIrBuilder,
        declarationParent: IrDeclarationParent,
        bodyBuilder: (factoryFun: org.jetbrains.kotlin.ir.declarations.IrSimpleFunction) -> IrExpression,
    ): IrFunctionExpressionImpl {
        val factoryName = Name.identifier("previewFactory_${factoryLambdaCounter++}")
        val lambdaFun = pluginContext.irFactory.buildFun {
            startOffset = SYNTHETIC_OFFSET
            endOffset = SYNTHETIC_OFFSET
            name = factoryName
            returnType = collectedPreviewType
            origin = IrDeclarationOriginCompat.LOCAL_FUNCTION_FOR_LAMBDA
            visibility = DescriptorVisibilities.LOCAL
        }.apply {
            parent = declarationParent
            body = DeclarationIrBuilder(pluginContext, symbol).irBlockBody {
                // Pass the lambda itself in as the `parent` for any nested IR (e.g. each
                // `CollectedPreview`'s `content = @Composable { ... }` lambda must be parented
                // here, not at the property getter — otherwise sibling content lambdas across
                // different factories all hash to `_get_previews_$0` under JVM lowering and
                // collide with "Platform declaration clash".
                +irReturn(bodyBuilder(this@apply))
            }
        }
        return IrFunctionExpressionImpl(
            startOffset = SYNTHETIC_OFFSET,
            endOffset = SYNTHETIC_OFFSET,
            type = factoryLambdaType,
            origin = IrStatementOrigin.LAMBDA,
            function = lambdaFun,
        )
    }

    /**
     * Builds `lazyPreviewSequence(*factories)`. Empty [factories] still emits a call —
     * `lazyPreviewSequence()` returns an empty `Sequence<CollectedPreview>` at runtime.
     */
    private fun buildLazyPreviewSequenceCall(builder: DeclarationIrBuilder, factories: List<IrExpression>): IrExpression {
        val vararg: IrVararg = IrVarargImpl(
            startOffset = SYNTHETIC_OFFSET,
            endOffset = SYNTHETIC_OFFSET,
            type = pluginContext.irBuiltIns.arrayClass.typeWith(factoryLambdaType),
            varargElementType = factoryLambdaType,
            elements = factories.toMutableList(),
        )

        return compatContext.irCall(
            builder,
            lazyPreviewSequenceFun,
            sequenceOfCollectedPreviewType,
        ).apply {
            arguments[0] = vararg
        }
    }

    // ----- PreviewExport wrapper -----

    private val previewExportClass by lazy {
        pluginContext.referenceClass(
            classIdOf("me.tbsten.compose.preview.lab", "PreviewExport"),
        ).orThrow { PreviewExportNotFoundError() }
    }

    private val previewExportType by lazy {
        previewExportClass.typeWith()
    }

    /**
     * Builds the IR for `PreviewExport(<lazyExpr>)` where [lazyExpr] is a
     * `Lazy<Sequence<CollectedPreview>>`. The backing field of properties declared as
     * `val x by collectModulePreviews()` / `val x by collectAllModulePreviews()` ends up
     * holding the resulting `PreviewExport` instance; the property's `getValue` then returns
     * the `PreviewExport` itself, so consumers pick `asList()` or `asSequence()` at the use
     * site.
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

    /**
     * Builds `lazy { valueExpr }` where [valueExpr] has type `Sequence<CollectedPreview>`.
     *
     * The lazy is needed because `valueExpr` is itself the
     * `lazyPreviewSequence({...}, ...)` call: while each `CollectedPreview` inside the
     * sequence is lazily realized per-element, the *act* of looking up
     * `lazyPreviewSequence` and constructing the factory-lambda array still runs at the
     * first `getValue`. Wrapping in `lazy` defers that one-shot setup to the property's
     * first access.
     */
    fun buildLazyCall(
        builder: DeclarationIrBuilder,
        valueExpr: IrExpression,
        declarationParent: IrDeclarationParent
    ): IrExpression {
        val lazyFun = pluginContext.referenceFunctions(
            callableIdOf("kotlin", "lazy"),
        ).first { fn ->
            fn.owner.parameters.filter { it.kind == IrParameterKind.Regular }.size == 1
        }

        val lambdaFun = pluginContext.irFactory.buildFun {
            startOffset = SYNTHETIC_OFFSET
            endOffset = SYNTHETIC_OFFSET
            name = SpecialNames.ANONYMOUS
            returnType = sequenceOfCollectedPreviewType
            origin = IrDeclarationOriginCompat.LOCAL_FUNCTION_FOR_LAMBDA
            visibility = DescriptorVisibilities.LOCAL
        }.apply {
            parent = declarationParent
            body = DeclarationIrBuilder(pluginContext, symbol).irBlockBody {
                +irReturn(valueExpr)
            }
        }

        val lambdaType = pluginContext.irBuiltIns.functionN(0).typeWith(sequenceOfCollectedPreviewType)

        val lambdaExpr = IrFunctionExpressionImpl(
            startOffset = SYNTHETIC_OFFSET,
            endOffset = SYNTHETIC_OFFSET,
            type = lambdaType,
            origin = IrStatementOrigin.LAMBDA,
            function = lambdaFun,
        )

        val lazyClass = pluginContext.referenceClass(
            classIdOf("kotlin", "Lazy"),
        ).orThrow { StdlibClassNotFoundError("kotlin.Lazy") }
        return compatContext.irCall(
            builder,
            lazyFun,
            lazyClass.typeWith(sequenceOfCollectedPreviewType),
            listOf(sequenceOfCollectedPreviewType),
        ).apply {
            arguments[0] = lambdaExpr
        }
    }

    // ----- Cross-module concatenation -----

    /**
     * Lazily-cached lookup of `me.tbsten.compose.preview.lab.distinctPreviewsByIdSequence`.
     */
    private val distinctPreviewsByIdSequenceFun by lazy {
        val callableId = callableIdOf("me.tbsten.compose.preview.lab", "distinctPreviewsByIdSequence")
        pluginContext.referenceFunctions(callableId).firstOrNull()
            .orThrow { RuntimeFunctionNotFoundError(callableId) }
    }

    /**
     * Lazily-cached per-declaration hint functions, keyed by collection scope.
     *
     * Each entry is an [IrSimpleFunction] with the signature
     * `previewHint_<scope>(value: PreviewHintMarker_<hash>?): CollectedPreview`. In
     * [buildConcatenatedPreviewsExpr] the marker argument exists only to disambiguate the
     * IdSignature, so `null` is passed and each hint contributes one `CollectedPreview`
     * factory wrapping `previewHint_<scope>(null)`.
     */
    private val cachedHintsByScope: MutableMap<String, List<IrSimpleFunction>> = mutableMapOf()

    private fun hintsForScope(scope: String): List<IrSimpleFunction> =
        cachedHintsByScope.getOrPut(scope) { discoverHints(pluginContext, compatContext, scope) }

    /**
     * Builds the cross-module concatenation expression for `collectAllModulePreviews()`.
     *
     * **Generated IR (semantically equivalent)**:
     *
     * ```kotlin
     * distinctPreviewsByIdSequence(
     *     lazyPreviewSequence(
     *         // this module's @Preview factories
     *         { CollectedPreview("id1", ...) },
     *         { CollectedPreview("id2", ...) },
     *         // dep-module hint factories (one per discovered hint)
     *         { previewHint_<scope>(null) },
     *         { previewHint_<scope>(null) },
     *     )
     * )
     * ```
     *
     * Each cross-module factory wraps `previewHint_<scope>(null)` so the dep-side
     * `CollectedPreview(...)` constructor is invoked only when the consumer's iteration
     * reaches that element (e.g. `previews.asSequence().firstOrNull { ... }` stops as soon
     * as a hit is found). `distinctPreviewsByIdSequence` then folds duplicates by id while
     * preserving encounter order.
     *
     * `distinctPreviewsByIdSequence` is needed because a dependency that itself uses
     * `collectAllModulePreviews()` re-exports its transitive previews. Without dedup, an
     * `app(all) → ui(all) → core(single)` chain would yield each `core` preview twice
     * (once via `core`'s hint, once via `ui`'s hint).
     */
    fun buildConcatenatedPreviewsExpr(
        builder: DeclarationIrBuilder,
        parent: IrDeclarationParent,
        scope: String,
    ): IrExpression {
        val hints = hintsForScope(scope)
        val scopedPreviews = previews.filter { scope in it.scopes }

        val thisModuleFactories = scopedPreviews.map { previewInfo ->
            buildPreviewFactoryLambda(builder, parent) { factoryFun ->
                previewBuilder.buildCollectedPreviewCall(previewInfo, builder, factoryFun)
            }
        }

        val hintFactories = hints.map { hintFn ->
            buildPreviewFactoryLambda(builder, parent) { _ ->
                val markerParam = hintFn.parameters.firstOrNull { it.kind == IrParameterKind.Regular }
                compatContext.irCall(builder, hintFn.symbol, collectedPreviewType).apply {
                    if (markerParam != null) {
                        arguments[0] = IrConstImpl.constNull(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, markerParam.type)
                    }
                }
            }
        }

        val sequenceExpr = buildLazyPreviewSequenceCall(builder, thisModuleFactories + hintFactories)
        return compatContext.irCall(builder, distinctPreviewsByIdSequenceFun, sequenceOfCollectedPreviewType).apply {
            arguments[0] = sequenceExpr
        }
    }
}
