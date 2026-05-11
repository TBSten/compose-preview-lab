package me.tbsten.compose.preview.lab.compiler.feature.previewCollection

import org.jetbrains.kotlin.GeneratedDeclarationKey

/**
 * Keys identifying declarations synthesized by the Compose Preview Lab compiler plugin
 * for the `previewCollection` feature.
 *
 * Each key marks the boundary between the FIR-side declaration and the IR-side body
 * filler that populates it — the FIR generator stamps the declaration with the matching
 * key, and the IR transformer dispatches on it to attach the body.
 *
 * Pattern adapted from Metro
 * (https://github.com/ZacSweers/metro/blob/main/compiler/src/main/kotlin/dev/zacsweers/metro/compiler/fir/Keys.kt).
 */
internal object PreviewKeys {
    /**
     * Identifies the hint function generated once per `@Preview` (one per scope, sharing
     * a marker class parameter for IdSignature disambiguation).
     *
     * **FIR side** declares the stub:
     * ```kotlin
     * // package me.tbsten.compose.preview.lab.hints
     * public fun previewHint_<scope>(
     *     value: PreviewHintMarker_<sanitized_fqn>_<hash>?,
     * ): CollectedPreview
     * ```
     *
     * **IR side** matches on this key and fills the body with the corresponding
     * `CollectedPreview(...)` constructor call.
     */
    object PreviewLabHint : GeneratedDeclarationKey()

    /**
     * Identifies the marker interface generated once per `@Preview`. Used as the
     * parameter type of the hint function; its sole purpose is to make the KLIB
     * IdSignature unique per `@Preview`. Emitted as an `interface` with
     * `Modality.ABSTRACT` to avoid IC collisions.
     *
     * **FIR side** emits:
     * ```kotlin
     * // package me.tbsten.compose.preview.lab.hints
     * @Deprecated("...", level = HIDDEN)
     * @InternalComposePreviewLabApi
     * @SyntheticPreviewHint
     * public interface PreviewHintMarker_<sanitized_fqn>_<hash>
     * ```
     */
    object PreviewLabHintMarkerInterface : GeneratedDeclarationKey()
}
