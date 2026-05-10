@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.feature.previewCollection.ir.collectPreviewsReplacement.buildPreviewSequence

import me.tbsten.compose.preview.lab.compiler.PluginConfig
import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.compat.IrDeclarationOriginCompat
import me.tbsten.compose.preview.lab.compiler.error.RuntimeFunctionNotFoundError
import me.tbsten.compose.preview.lab.compiler.error.throwAsException
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.PreviewFunctionInfo
import me.tbsten.compose.preview.lab.compiler.utils.callableIdOf
import me.tbsten.compose.preview.lab.compiler.utils.classIdOf
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.name.Name

/**
 * Builds the `Sequence<CollectedPreview>` IR for `val x by collectModulePreviews()`
 * (same-module only) — the orchestrating builder for the `buildPreviewSequence/`
 * sub-logic.
 *
 * **Sample call → resulting IR**:
 * ```kotlin
 * BuildPreviewSequenceIr(pluginContext, previews, config, compatContext)
 *     .invoke(builder, parent, scope = "default")
 * // result IR ≡
 * //   lazyPreviewSequence(
 * //       { CollectedPreview("My1", ..., content = { My1() }) },
 * //       { CollectedPreview("My2", ..., content = { My2() }) },
 * //   )
 * ```
 *
 * Each `CollectedPreview(...)` constructor call is wrapped in a `() -> CollectedPreview`
 * factory lambda so iteration via `asSequence().take(n)` does not allocate the
 * `@Composable` lambda for previews the consumer never reads.
 *
 * **Shared state** ([context]) — the lazy lookups for `lazyPreviewSequence`,
 * `kotlin.sequences.Sequence` and the `CollectedPreview` builder are owned by this class
 * and re-exposed to the sibling sub-builders ([BuildLazyWrapperIr],
 * [BuildPreviewExportIr], [BuildConcatenatedPreviewSequencesIr]). Constructing this
 * class once per IR transformer (= per module) keeps all lookups cached.
 */
internal class BuildPreviewSequenceIr(
    private val pluginContext: IrPluginContext,
    private val previews: List<PreviewFunctionInfo>,
    @Suppress("unused") private val config: PluginConfig,
    private val compatContext: CompatContext,
) {

    /** Shared context exposed to sibling sub-builders. */
    internal val context: PreviewSequenceBuildContext = PreviewSequenceBuildContext(pluginContext, compatContext)

    /**
     * Same-module preview sequence: `lazyPreviewSequence({...}, ...)` for the previews
     * whose [PreviewFunctionInfo.scopes] contains [scope].
     *
     * **Empty-case sample**: with no preview matching the scope, emits
     * `lazyPreviewSequence()` (no factories), which returns an empty sequence at runtime.
     */
    operator fun invoke(builder: DeclarationIrBuilder, parent: IrDeclarationParent, scope: String): IrExpression {
        val scopedPreviews = previews.filter { scope in it.scopes }
        val factories = scopedPreviews.map { previewInfo ->
            context.buildPreviewFactoryLambda(builder, parent) { factoryFun ->
                context.previewBuilder(previewInfo, builder, factoryFun)
            }
        }
        return context.buildLazyPreviewSequenceCall(builder, factories)
    }
}

/**
 * Shared state used by every `Build*Ir` class inside `buildPreviewSequence/`. Owns the
 * one-off lazy lookups (`lazyPreviewSequence` runtime symbol, `Sequence<CollectedPreview>`
 * type) so the sub-builders can share allocations without forcing them to be inner
 * classes.
 *
 * Constructed once per IR transformer via [BuildPreviewSequenceIr.context].
 */
internal class PreviewSequenceBuildContext(val pluginContext: IrPluginContext, val compatContext: CompatContext,) {
    val previewBuilder: BuildCollectedPreviewIr = BuildCollectedPreviewIr(pluginContext, compatContext)

    val collectedPreviewType: IrType get() = previewBuilder.collectedPreviewType

    val sequenceOfCollectedPreviewType: IrType by lazy {
        pluginContext.referenceClass(
            classIdOf("kotlin.sequences", "Sequence"),
        )!!.typeWith(collectedPreviewType)
    }

    /** `() -> CollectedPreview` factory-lambda type used for the `lazyPreviewSequence` vararg. */
    val factoryLambdaType: IrType by lazy {
        pluginContext.irBuiltIns.functionN(0).typeWith(collectedPreviewType)
    }

    val lazyPreviewSequenceFun by lazy {
        val callableId = callableIdOf("me.tbsten.compose.preview.lab", "lazyPreviewSequence")
        pluginContext.referenceFunctions(callableId).firstOrNull()
            ?: RuntimeFunctionNotFoundError(callableId).throwAsException()
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
     * `Composable` content lambda inside `CollectedPreview` is therefore not allocated
     * until the sequence iteration reaches this element.
     */
    fun buildPreviewFactoryLambda(
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
    fun buildLazyPreviewSequenceCall(builder: DeclarationIrBuilder, factories: List<IrExpression>): IrExpression {
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
}
