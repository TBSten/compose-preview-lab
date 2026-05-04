package me.tbsten.compose.preview.lab.compiler.fir

import org.jetbrains.kotlin.GeneratedDeclarationKey

/**
 * Keys identifying declarations synthesized by the Compose Preview Lab compiler plugin.
 *
 * Will be used by the FIR declaration generator (planned in a follow-up PR) to mark
 * generated symbols, and by the IR body filler to recognize them when injecting body
 * expressions. Currently introduced as part of the foundational scaffolding for the
 * FIR-based hint generation pipeline.
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

    /**
     * Marks `previewLabAutoProvider_<hash>(): List<CollectedPreview>` provider stubs emitted
     * by [PreviewLabHintFirGenerator] in the leaf source-set's FIR session.
     *
     * The body is filled in IR by [me.tbsten.compose.preview.lab.compiler.ir.PreviewLabHintIrBodyFiller]
     * with the concatenated module previews + dependency previews. Emitting the stub through
     * FIR (instead of building a fresh `IrSimpleFunction` post-hoc in IR) is what gives the
     * function a proper KLIB IdSignature: FIR-declared top-level callables go through the
     * compiler's standard declaration pipeline, so consumer-side
     * `referenceFunctions(callableId)` lookups land on a symbol whose IdSignature matches
     * what the consumer's IR builder computes for the call. IR-only `irFactory.buildFun`
     * additions land in the KLIB strings table but never participate in the IdSignature
     * symbol table the linker resolves against, surfacing as
     * `IrLinkageError: No function found for symbol previewLabAutoProvider_<hash>` at link
     * time.
     */
    object PreviewLabAutoProvider : GeneratedDeclarationKey()
}
