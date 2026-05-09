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
 * **Trust model — defence-in-depth, not airtight**. The annotation is publicly visible so
 * the compiler plugin's FIR generator can attach it from outside the runtime module; that
 * means a determined third party who explicitly opts in to
 * `@InternalComposePreviewLabApi` (a documented "you signed up for this" gate) can still
 * apply `@SyntheticPreviewHint` to their own declarations. Going further would require
 * either a private capability passed at FIR-generation time or a checker that walks every
 * usage of the annotation — both add complexity for an attack surface that requires
 * explicit opt-in to internal API. The accidental-injection scenario (a transitive
 * dependency that happens to declare the same package by mistake) is fully blocked, which
 * is the case the discovery guard exists to handle.
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
