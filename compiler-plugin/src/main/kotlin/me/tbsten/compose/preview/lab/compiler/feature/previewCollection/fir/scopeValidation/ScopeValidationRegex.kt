package me.tbsten.compose.preview.lab.compiler.feature.previewCollection.fir.scopeValidation

/**
 * Validation rule for `collectScope` values.
 *
 * The scope is embedded directly into a Kotlin identifier (`previewHint_<scope>`),
 * so anything outside `[A-Za-z0-9_]+` would either fail to compile or, worse,
 * accidentally land on an unrelated function name. Validating up front lets us
 * surface the mistake as a clear diagnostic instead.
 *
 * Both [CheckCollectScopeAnnotation] / [CheckCollectScopeCall] (FIR analysis phase)
 * and `ReplaceCollectPreviewsFunBody` (IR phase — second-line defence for `const val`
 * references that the FIR Checker cannot distinguish from non-`const` vals) reach
 * for this regex. The IR-side caller imports it across logic boundaries because the
 * validation rule is genuinely shared between FIR Checker and IR const-folding seam.
 */
internal val SCOPE_VALIDATION_REGEX: Regex = Regex("[A-Za-z0-9_]+")
