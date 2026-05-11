@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.feature.previewCollection.ir.collectPreviewsReplacement.buildPreviewSequence

import me.tbsten.compose.preview.lab.compiler.error.RuntimeFunctionNotFoundError
import me.tbsten.compose.preview.lab.compiler.error.throwAsException
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.PreviewFunctionInfo
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.ir.collectPreviewsReplacement.discoverHints
import me.tbsten.compose.preview.lab.compiler.utils.callableIdOf
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET

/**
 * Builds the cross-module concatenation expression for
 * `val x by collectAllModulePreviews()`.
 *
 * **Sample call → resulting IR**:
 * ```kotlin
 * BuildConcatenatedPreviewSequencesIr(context, previews)
 *     .invoke(builder, parent, scope = "design")
 * // result IR ≡
 * //   distinctPreviewsByIdSequence(
 * //       lazyPreviewSequence(
 * //           // this module's @Preview factories
 * //           { CollectedPreview("id1", ...) },
 * //           { CollectedPreview("id2", ...) },
 * //           // dep-module hint factories (one per discovered hint)
 * //           { previewHint_design(null) },
 * //           { previewHint_design(null) },
 * //       )
 * //   )
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
internal class BuildConcatenatedPreviewSequencesIr(
    private val context: PreviewSequenceBuildContext,
    private val previews: List<PreviewFunctionInfo>,
) {

    private val distinctPreviewsByIdSequenceFun by lazy {
        val callableId = callableIdOf("me.tbsten.compose.preview.lab", "distinctPreviewsByIdSequence")
        context.pluginContext.referenceFunctions(callableId).firstOrNull()
            ?: RuntimeFunctionNotFoundError(callableId).throwAsException()
    }

    /**
     * Lazily-cached per-declaration hint functions, keyed by collection scope.
     *
     * Each entry is an [IrSimpleFunction] with the signature
     * `previewHint_<scope>(value: PreviewHintMarker_<hash>?): CollectedPreview`. The
     * marker argument exists only to disambiguate the IdSignature; its value is never
     * inspected, so `null` is passed at the call site and each hint contributes one
     * `CollectedPreview` factory.
     */
    private val cachedHintsByScope: MutableMap<String, List<IrSimpleFunction>> = mutableMapOf()

    private fun hintsForScope(scope: String): List<IrSimpleFunction> = cachedHintsByScope.getOrPut(scope) {
        discoverHints(context.pluginContext, context.compatContext, scope)
    }

    operator fun invoke(builder: DeclarationIrBuilder, parent: IrDeclarationParent, scope: String,): IrExpression {
        val hints = hintsForScope(scope)
        val scopedPreviews = previews.filter { scope in it.scopes }

        val thisModuleFactories = scopedPreviews.map { previewInfo ->
            context.buildPreviewFactoryLambda(builder, parent) { factoryFun ->
                context.previewBuilder(previewInfo, builder, factoryFun)
            }
        }

        val hintFactories = hints.map { hintFn ->
            context.buildPreviewFactoryLambda(builder, parent) { _ ->
                val markerParam = hintFn.parameters.firstOrNull { it.kind == IrParameterKind.Regular }
                context.compatContext.irCall(builder, hintFn.symbol, context.collectedPreviewType).apply {
                    if (markerParam != null) {
                        arguments[0] = IrConstImpl.constNull(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, markerParam.type)
                    }
                }
            }
        }

        val sequenceExpr = context.buildLazyPreviewSequenceCall(builder, thisModuleFactories + hintFactories)
        return context.compatContext.irCall(builder, distinctPreviewsByIdSequenceFun, context.sequenceOfCollectedPreviewType)
            .apply {
                arguments[0] = sequenceExpr
            }
    }
}
