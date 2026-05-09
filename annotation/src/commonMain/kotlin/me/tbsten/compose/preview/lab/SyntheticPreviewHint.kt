package me.tbsten.compose.preview.lab

/**
 * Marker annotation attached by the Compose Preview Lab compiler plugin to every synthesized
 * `previewHint_<scope>` overload and `PreviewHintMarker_*` class. The IR-side hint discovery
 * (`HintDiscovery.discoverHints`) requires this marker on candidate hint declarations before
 * accepting them — declarations that share the
 * `me.tbsten.compose.preview.lab.hints` package but lack `@SyntheticPreviewHint` are rejected
 * with a compile-time warning so a third-party library cannot squat the hint package and
 * inject its own previews into a downstream consumer's `collectAllModulePreviews()`.
 *
 * Internal API — never attach manually. The annotation is emitted exclusively by
 * `PreviewHintFirGenerator` at FIR-generation time. It carries `@InternalComposePreviewLabApi`
 * so user code that tries to reference it gets the standard internal-API opt-in error rather
 * than a less-helpful "unresolved reference".
 */
@InternalComposePreviewLabApi
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class SyntheticPreviewHint
