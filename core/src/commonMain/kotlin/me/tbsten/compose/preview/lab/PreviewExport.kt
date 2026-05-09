package me.tbsten.compose.preview.lab

import kotlin.reflect.KProperty

/**
 * Wrapper around a lazy list of [CollectedPreview] used as the backing field type for properties
 * declared with `val x by collectModulePreviews()` / `collectAllModulePreviews()`.
 *
 * The lazy wrapping ensures that `@Composable` lambdas inside [CollectedPreview] are initialized
 * on first access rather than at class-load time, avoiding `ExceptionInInitializerError` in JVM
 * static initializers.
 *
 * Cross-module preview discovery does **not** rely on this type — it is handled by the compiler
 * plugin emitting per-`@Preview` hint functions
 * (`previewHint_<scope>(value: PreviewHintMarker_<hash>?): CollectedPreview` in the
 * `me.tbsten.compose.preview.lab.hints` package) that the consumer side resolves through
 * `IrPluginContext.referenceFunctions`.
 *
 * Users do not construct this class directly — it is produced exclusively by the compiler plugin.
 *
 * @see collectModulePreviews
 * @see collectAllModulePreviews
 */
public class PreviewExport
@InternalComposePreviewLabApi
constructor(private val delegate: Lazy<List<CollectedPreview>>,) {
    /** The collected previews. Forces the underlying [Lazy]. */
    public val value: List<CollectedPreview> get() = delegate.value

    /** Property delegate accessor so that `val x by collectModulePreviews()` resolves to a `List`. */
    public operator fun getValue(thisRef: Any?, property: KProperty<*>): List<CollectedPreview> = delegate.value
}
