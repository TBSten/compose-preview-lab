package me.tbsten.compose.preview.lab

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Property delegate for `val x by collectModulePreviews()` / `collectAllModulePreviews()`.
 * Reading the property yields the underlying `Sequence<CollectedPreview>` directly, so
 * consumers iterate with the standard `kotlin.sequences` operators (`firstOrNull { ... }`,
 * `take(N).toList()`, `toList()`, …) without going through any wrapper accessor.
 *
 * The sequence is wrapped in a [Lazy] so that the per-`@Preview` factory lambdas the
 * compiler plugin emits are not invoked until the property is first read. See
 * `core/docs/cross-module-aggregation.md` for the laziness rationale and the
 * discovery mechanism (which does **not** flow through this type — it is handled by the
 * compiler plugin's per-`@Preview` hint functions resolved via
 * `IrPluginContext.referenceFunctions`).
 *
 * Users do not construct this class directly — it is produced exclusively by the compiler
 * plugin.
 *
 * ```kotlin
 * val myPreviews by collectModulePreviews()
 * myPreviews.toList()                                // eager drain
 * myPreviews.firstOrNull { it.id == targetId }       // lazy early-exit
 * ```
 *
 * @see collectModulePreviews
 * @see collectAllModulePreviews
 */
public class PreviewExport
@InternalComposePreviewLabApi
constructor(private val delegate: Lazy<Sequence<CollectedPreview>>,) :
    ReadOnlyProperty<Any?, Sequence<CollectedPreview>> {
    /**
     * Returns the underlying [Sequence], forcing the [Lazy] but **not** materialising each
     * [CollectedPreview]. The same `Sequence` instance is returned on every property read.
     * Whether that sequence can be iterated repeatedly is an implementation property of
     * `Sequence` and is not guaranteed by this type — call `toList()` once if you need a
     * snapshot you can scan repeatedly.
     */
    public override operator fun getValue(thisRef: Any?, property: KProperty<*>): Sequence<CollectedPreview> = delegate.value
}
