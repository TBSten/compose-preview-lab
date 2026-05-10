@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.ir

import me.tbsten.compose.preview.lab.compiler.PreviewLabConstants
import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.PreviewFunctionInfo
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.PreviewKeys
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.buildPreviewHintCanonicalKey
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.computeHintHash
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.parameterTypeFqns
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
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

/**
 * Fills the body of the
 * `previewHint(value: PreviewHintMarker_<sanitized_fqn>_<hash>?): CollectedPreview`
 * stubs emitted by
 * [me.tbsten.compose.preview.lab.compiler.fir.PreviewHintFirGenerator] so that they
 * return their corresponding `CollectedPreview`. FIR cannot own a body, so this IR pass
 * rewrites each stub into an `irReturn` of the `CollectedPreview(...)` constructor call.
 *
 * **Hint function**
 *
 * Before (handed down from FIR):
 * ```kotlin
 * public fun previewHint(value: PreviewHintMarker_<sanitized_fqn>_<hash>?): CollectedPreview  // body == null
 * ```
 *
 * After (rewritten by this transformer):
 * ```kotlin
 * public fun previewHint(value: PreviewHintMarker_<sanitized_fqn>_<hash>?): CollectedPreview = CollectedPreview(
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
 *   against the canonical-key hash of each `@Preview` in the IR module. FIR generator and
 *   IR side share [computeHintHash] / [buildPreviewHintCanonicalKey], so the match is
 *   unambiguous.
 * - **`CollectedPreview` construction**: reuses the existing
 *   [CollectedPreviewIrBuilder.buildCollectedPreviewCall] unchanged.
 * - **`@ComposePreviewLabOption(ignore = true)` handling**: the FIR generator filters
 *   ignored previews out *before* emitting any hint declaration, so the IR-side body
 *   filler does the same — `previewsByHash` is built from the filtered `previews` list,
 *   and the few hint stubs that exist after the FIR pass are guaranteed to have a
 *   matching entry. This also keeps ignored previews out of the hash collision check
 *   (avoiding false positives between an ignored preview and an emitted preview that
 *   share the same truncated SHA-256 hash).
 */
internal class PreviewHintIrBodyFiller(
    private val pluginContext: IrPluginContext,
    private val compatContext: CompatContext,
    private val previewsByHash: Map<String, PreviewFunctionInfo>,
) : IrElementTransformerVoid() {

    /** Lazily built; reuses the existing builder to emit `CollectedPreview(...)` IR. */
    private val collectedPreviewBuilder by lazy {
        CollectedPreviewIrBuilder(pluginContext, compatContext)
    }

    /**
     * Rewrites the hint function body to an `irReturn` of the `CollectedPreview(...)`
     * constructor call.
     *
     * Only functions with the `PreviewKeys.PreviewLabHint` origin are touched. The original
     * `@Preview` is identified from the marker class short name on the hint's
     * `value: PreviewHintMarker_<sanitized_fqn>_<hash>?` parameter.
     *
     * **Before**:
     * ```kotlin
     * public fun previewHint(value: PreviewHintMarker_uilib_button_MyButton_a3k9z2x1?): CollectedPreview  // body == null
     * ```
     *
     * **After** (semantically):
     * ```kotlin
     * public fun previewHint(value: PreviewHintMarker_uilib_button_MyButton_a3k9z2x1?): CollectedPreview = CollectedPreview(
     *     id = "uiLib.button.MyButton", ..., content = @Composable { uiLib.button.MyButton() },
     * )
     * ```
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
     * constructor call.
     *
     * The original `@Preview` is recovered from the marker class name on the hint's
     * `value: PreviewHintMarker_<sanitized_fqn>_<hash>` parameter (the FIR generator
     * embeds the hash there).
     */
    private fun fillHintBody(declaration: IrSimpleFunction) {
        val regularParams = declaration.parameters.filter { it.kind == IrParameterKind.Regular }
        if (regularParams.size != 1) return
        val markerFqn = regularParams[0].type.classFqName ?: return
        val markerShortName = markerFqn.shortName().asString()
        if (!markerShortName.startsWith(PreviewLabConstants.PreviewHintMarkerPrefix)) return
        // The marker name is `PreviewHintMarker_<sanitized_fqn>_<hash>`. The hash is a
        // fixed-length ([PreviewLabConstants.HashLength]) base-36 string, so a tail
        // slice recovers it.
        val hash = markerShortName.takeLast(PreviewLabConstants.HashLength)
        val previewInfo = previewsByHash[hash] ?: return

        val builder = DeclarationIrBuilder(pluginContext, declaration.symbol)
        declaration.body = builder.irBlockBody {
            +irReturn(
                collectedPreviewBuilder.buildCollectedPreviewCall(
                    preview = previewInfo,
                    builder = builder,
                    parent = declaration,
                ),
            )
        }
    }
}

/**
 * Walks every `@Preview`-annotated top-level function in the module fragment and builds a
 * map from canonical-key hash to [PreviewFunctionInfo].
 *
 * Callers should pass an already-filtered `previews` list (i.e. with
 * `@ComposePreviewLabOption(ignore = true)` removed). The FIR generator filters ignored
 * previews out before emitting any hint declaration, so this map only needs entries for
 * the previews that have a corresponding emitted hint stub. Including ignored previews
 * here would also bloat the hash-collision check (raising the chance of a false-positive
 * ERROR between an ignored preview and an emitted preview that share a truncated hash).
 *
 * The canonical key includes both `sourceFqn` and the parameter-type FQNs so that
 * same-name overloads (`fun MyButton()` vs `fun MyButton(text: String)`) are
 * disambiguated. [buildPreviewHintCanonicalKey] is shared with the FIR generator to keep
 * the format identical on both sides.
 *
 * **Sample entry**: `"a3k9z2x1" → PreviewFunctionInfo(function = fun MyButton(), id = "uiLib.button.MyButton", ...)`
 *
 * # Collision detection
 *
 * The hint hash is SHA-256 truncated to 8 base-36 characters (~41 bits; see
 * [computeHintHash]), so distinct canonical keys can in principle collide (~10⁻⁷ at 1k
 * previews). On collision a naive `put` would silently drop one `@Preview` from the
 * aggregated result, so the [onCollision] callback notifies the caller, who reports an
 * ERROR.
 *
 * Detection compares **canonical keys directly**, not FQN, so same-name overloads cannot
 * be misclassified. Re-registering the same canonical key is not a collision and silently
 * overwrites.
 */
internal fun buildPreviewByHashMap(
    previews: List<PreviewFunctionInfo>,
    onCollision: (hash: String, existing: PreviewFunctionInfo, conflicting: PreviewFunctionInfo) -> Unit = { _, _, _ -> },
): Map<String, PreviewFunctionInfo> {
    val keyed = previews.mapNotNull { preview ->
        val sourceFqn = preview.function.kotlinFqName.asString()
        if (sourceFqn.isEmpty()) return@mapNotNull null
        val parameterTypeFqns = preview.function.parameterTypeFqns()
        val canonicalKey = buildPreviewHintCanonicalKey(sourceFqn, parameterTypeFqns)
        canonicalKey to preview
    }
    return buildHashMapWithCollisionDetection(keyed, ::computeHintHash, onCollision)
}

/**
 * Generic helper that turns `(canonical key, value)` pairs into a `hash → value` map and
 * fires [onCollision] when two distinct canonical keys land on the same hash. Re-using
 * the same canonical key for the same hash is treated as an idempotent overwrite.
 *
 * Extracted from [buildPreviewByHashMap] so unit tests can inject a controlled [hash]
 * function (e.g. one that always returns the same hash for two distinct keys) without
 * needing real `IrSimpleFunction` instances.
 *
 * **Sample (collision case)**:
 * ```kotlin
 * val hits = mutableListOf<String>()
 * val map = buildHashMapWithCollisionDetection(
 *     entries = listOf("keyA" to "A", "keyB" to "B"),
 *     hash = { "h" }, // force collision
 *     onCollision = { _, existing, conflicting -> hits += "$existing/$conflicting" },
 * )
 * // map = { "h" -> "B" }, hits = ["A/B"]
 * ```
 */
internal fun <V> buildHashMapWithCollisionDetection(
    entries: List<Pair<String, V>>,
    hash: (canonicalKey: String) -> String,
    onCollision: (hash: String, existing: V, conflicting: V) -> Unit = { _, _, _ -> },
): Map<String, V> = buildMap {
    val canonicalKeyByHash = mutableMapOf<String, String>()
    for ((canonicalKey, value) in entries) {
        val h = hash(canonicalKey)
        val existingKey = canonicalKeyByHash[h]
        if (existingKey != null && existingKey != canonicalKey) {
            // Two distinct canonical keys landed on the same hash = a genuine collision.
            // Re-registering the same canonical key (e.g. a file walked twice across the
            // ignore=true filter passes) is just noise, so silently overwrite.
            val existing = getValue(h)
            onCollision(h, existing, value)
        }
        canonicalKeyByHash[h] = canonicalKey
        put(h, value)
    }
}
