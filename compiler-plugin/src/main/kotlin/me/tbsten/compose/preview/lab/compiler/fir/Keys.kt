package me.tbsten.compose.preview.lab.compiler.fir

import org.jetbrains.kotlin.GeneratedDeclarationKey

/**
 * Keys identifying declarations synthesized by the Compose Preview Lab compiler plugin.
 *
 * Used by the FIR declaration generator to mark generated symbols and by the IR body
 * filler to recognize them when injecting body expressions.
 *
 * Pattern adapted from Metro
 * (https://github.com/ZacSweers/metro/blob/main/compiler/src/main/kotlin/dev/zacsweers/metro/compiler/fir/Keys.kt).
 */
internal object Keys {
    /**
     * Marks `previewLabExport(value: <Marker>): Unit` hint functions emitted in
     * `me.tbsten.compose.preview.lab.exports`.
     *
     * Each module that contains `@Preview` functions or a `collectModulePreviews()` /
     * `collectAllModulePreviews()` delegate emits one such hint per export target so that
     * downstream `collectAllModulePreviews()` can find them via `referenceFunctions`.
     */
    object PreviewLabHint : GeneratedDeclarationKey()

    /**
     * Marks the synthetic marker classes used as the `value` parameter type of
     * [PreviewLabHint] functions.
     *
     * The marker class is what makes per-export hints have a unique KLIB IdSignature
     * (`(name, parameter types)` differ between modules even though the function name is
     * always `previewLabExport`).
     */
    object PreviewLabHintMarker : GeneratedDeclarationKey()
}
