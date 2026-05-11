@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.feature.previewCollection.ir.collectPreviewsReplacement

import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.PreviewFunctionInfo
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.PreviewKeys
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.extractHashFromMarkerShortName
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.isMarkerShortName
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.ir.collectPreviewsReplacement.buildPreviewSequence.BuildCollectedPreviewIr
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

/**
 * IR transformer that fills the body of the
 * `previewHint_<scope>(value: PreviewHintMarker_<sanitized_fqn>_<hash>?): CollectedPreview`
 * stubs emitted by the FIR-side hint generator. FIR cannot own a body, so this IR pass
 * rewrites each stub into an `irReturn` of the corresponding `CollectedPreview(...)`
 * constructor call.
 *
 * **Before** (handed down from FIR):
 * ```kotlin
 * public fun previewHint_default(value: PreviewHintMarker_<sanitized_fqn>_<hash>?): CollectedPreview  // body == null
 * ```
 *
 * **After** (rewritten by this transformer):
 * ```kotlin
 * public fun previewHint_default(value: PreviewHintMarker_<sanitized_fqn>_<hash>?): CollectedPreview = CollectedPreview(
 *     id = "uiLib.button.MyButton",
 *     displayName = "uiLib.button.MyButton",
 *     filePath = "uiLib/src/.../MyButton.kt",
 *     startLineNumber = 5,
 *     endLineNumber = 9,
 *     code = "{ ... }",
 *     kdoc = null,
 *     content = @Composable { uiLib.button.MyButton() },
 * )
 * ```
 *
 * # Design points
 *
 * - **Matching hint to `@Preview`**: extract the hash from the marker class short name on
 *   the hint's `value: PreviewHintMarker_<sanitized_fqn>_<hash>?` parameter and look it up
 *   in [previewsByHash] (built by [BuildPreviewByHashMap]). FIR and IR sides share the
 *   canonical-key hashing, so the match is unambiguous.
 * - **`CollectedPreview` construction**: reuses [BuildCollectedPreviewIr] unchanged.
 * - **`@ComposePreviewLabOption(ignore = true)` handling**: the FIR generator filters
 *   ignored previews out *before* emitting any hint declaration, so [previewsByHash] is
 *   built from the same filtered `previews` list — every hint stub is guaranteed to have
 *   a matching entry, and ignored previews stay out of the hash-collision check.
 */
internal class FillPreviewHintIrBody(
    private val pluginContext: IrPluginContext,
    private val compatContext: CompatContext,
    private val previewsByHash: Map<String, PreviewFunctionInfo>,
) : IrElementTransformerVoid() {

    /** Lazily built; reuses [BuildCollectedPreviewIr] to emit `CollectedPreview(...)` IR. */
    private val collectedPreviewBuilder by lazy {
        BuildCollectedPreviewIr(pluginContext, compatContext)
    }

    /**
     * Visitor entry point. Touches only functions with the [PreviewKeys.PreviewLabHint]
     * origin and a still-`null` body (= stubs handed down from the FIR generator).
     */
    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        if (declaration.body != null) return super.visitSimpleFunction(declaration)
        val origin = declaration.origin
        when {
            origin is IrDeclarationOrigin.GeneratedByPlugin && origin.pluginKey === PreviewKeys.PreviewLabHint -> {
                fillHintBody(declaration)
            }
        }
        return super.visitSimpleFunction(declaration)
    }

    /**
     * Rewrites the hint function body to an `irReturn` of the `CollectedPreview(...)`
     * constructor call. The original `@Preview` is recovered from the marker class name
     * on the hint's `value: PreviewHintMarker_<sanitized_fqn>_<hash>` parameter via
     * [extractHashFromMarkerShortName].
     */
    private fun fillHintBody(declaration: IrSimpleFunction) {
        val regularParams = declaration.parameters.filter { it.kind == IrParameterKind.Regular }
        if (regularParams.size != 1) return
        val markerFqn = regularParams[0].type.classFqName ?: return
        val markerShortName = markerFqn.shortName().asString()
        if (!isMarkerShortName(markerShortName)) return
        val hash = extractHashFromMarkerShortName(markerShortName)
        val previewInfo = previewsByHash[hash] ?: return

        val builder = DeclarationIrBuilder(pluginContext, declaration.symbol)
        declaration.body = builder.irBlockBody {
            +irReturn(
                collectedPreviewBuilder(
                    preview = previewInfo,
                    builder = builder,
                    parent = declaration,
                ),
            )
        }
    }
}
