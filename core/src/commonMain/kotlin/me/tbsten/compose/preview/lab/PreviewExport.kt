package me.tbsten.compose.preview.lab

import kotlin.reflect.KProperty

/**
 * Wrapper around a lazy list of [CollectedPreview] that the compiler plugin uses as a marker type
 * for cross-module preview discovery.
 *
 * Properties declared as `val x by collectModulePreviews()` end up with a backing field of type
 * [PreviewExport]. The compiler plugin emits a synthetic hint function in the
 * `me.tbsten.compose.preview.lab.exports` package whose parameter is also typed as [PreviewExport],
 * which makes downstream `collectAllModulePreviews()` callers able to discover the property through
 * `IrPluginContext.referenceFunctions(...)` without any manual Gradle configuration.
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
