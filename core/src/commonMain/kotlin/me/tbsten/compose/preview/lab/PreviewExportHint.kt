package me.tbsten.compose.preview.lab

/**
 * Internal annotation that the Compose Preview Lab compiler plugin attaches to synthetic hint
 * functions in the `me.tbsten.compose.preview.lab.exports` package.
 *
 * Each `val x by collectModulePreviews()` or `val x by collectAllModulePreviews()` declaration
 * produces a hint function whose `fqn` argument carries the fully-qualified name of the
 * original property (supporting both local collection and re-export chains).
 * Downstream `collectAllModulePreviews()` calls discover these hint functions via
 * `IrPluginContext.referenceFunctions(...)` and use [fqn] to resolve the original property.
 *
 * Users do not write this annotation directly — it is produced exclusively by the compiler plugin.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@InternalComposePreviewLabApi
public annotation class PreviewExportHint(val fqn: String)
