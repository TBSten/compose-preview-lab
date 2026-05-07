package me.tbsten.compose.preview.lab.compiler.fir

import org.jetbrains.kotlin.GeneratedDeclarationKey

/**
 * Keys identifying declarations synthesized by the Compose Preview Lab compiler plugin.
 *
 * Pattern adapted from Metro
 * (https://github.com/ZacSweers/metro/blob/main/compiler/src/main/kotlin/dev/zacsweers/metro/compiler/fir/Keys.kt).
 */
internal object Keys {
    /**
     * Identifies the hint function generated once per `@Preview`.
     *
     * For each `@Preview` function the FIR side declares a stub
     * `me.tbsten.compose.preview.lab.hints/previewHint(value: PreviewHintMarker_<sanitized_fqn>_<hash>): CollectedPreview`
     * and the IR side fills in the body. The fixed callable name plus the marker class
     * parameter are what make the IdSignature unique per `@Preview`.
     *
     * Used by:
     * - [PreviewHintFirGenerator] (FIR side): attaches this origin to the hint stub.
     * - [me.tbsten.compose.preview.lab.compiler.ir.PreviewHintIrBodyFiller] (IR side):
     *   matches functions by this key and fills their body with an `irReturn` of the
     *   corresponding `CollectedPreview(...)` constructor call.
     */
    object PreviewLabHint : GeneratedDeclarationKey()

    /**
     * Identifies the marker interface generated once per `@Preview`. Used as the parameter
     * type of the hint function; its sole purpose is to make the KLIB IdSignature unique
     * per `@Preview`. Emitted as an `interface` with `Modality.ABSTRACT` to avoid IC
     * collisions.
     */
    object PreviewLabHintMarker : GeneratedDeclarationKey()
}
