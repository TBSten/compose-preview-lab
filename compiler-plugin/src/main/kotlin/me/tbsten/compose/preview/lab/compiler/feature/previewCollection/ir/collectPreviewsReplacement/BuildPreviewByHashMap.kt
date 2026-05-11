@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.feature.previewCollection.ir.collectPreviewsReplacement

import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.PreviewFunctionInfo
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.buildPreviewHintCanonicalKey
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.computeHintHash
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.parameterTypeFqns
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.kotlinFqName

/**
 * Builds a `hash → [PreviewFunctionInfo]` map for the module's `@Preview` functions,
 * keyed by the same canonical-key hash that the FIR generator uses to name the marker
 * interface. [FillPreviewHintIrBody] consumes the resulting map to look up which
 * `@Preview` a hint stub belongs to.
 *
 * Callers should pass an already-filtered `previews` list (i.e. with
 * `@ComposePreviewLabOption(ignore = true)` removed). The FIR generator filters ignored
 * previews out before emitting any hint declaration, so this map only needs entries for
 * the previews that have a corresponding emitted hint stub. Including ignored previews
 * here would also bloat the hash-collision check (raising the chance of a false-positive
 * ERROR between an ignored preview and an emitted preview that share a truncated hash).
 *
 * **Sample call → resulting entry**:
 * ```kotlin
 * // @Preview fun MyButton(text: String)  // in module "uiLib.button"
 * BuildPreviewByHashMap(listOf(previewInfo)) { _, _, _ -> }
 *   // → { "a3k9z2x1" → PreviewFunctionInfo(function = fun MyButton(text: String), ...) }
 * ```
 *
 * # Collision detection
 *
 * Hash is SHA-256 truncated to 8 base-36 characters (~41 bits), so distinct canonical
 * keys can in principle collide (~10⁻⁷ at 1k previews). On collision a naive `put`
 * would silently drop one `@Preview` from the aggregated result, so the [onCollision]
 * callback notifies the caller, who reports an ERROR. Detection compares **canonical
 * keys directly**, not FQN, so same-name overloads cannot be misclassified. Re-registering
 * the same canonical key is treated as an idempotent overwrite (not a collision).
 *
 * Delegates the structural collision-detection bookkeeping to
 * [buildHashMapWithCollisionDetection]; this class injects the preview-specific canonical
 * key construction (`<sourceFqn>(<paramTypeFqns>)`).
 */
internal class BuildPreviewByHashMap {

    operator fun invoke(
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
}
