package me.tbsten.compose.preview.lab

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Property delegate (`ReadOnlyProperty<Any?, Sequence<CollectedPreview>>`) used as the
 * backing field type for properties declared with `val x by collectModulePreviews()` /
 * `collectAllModulePreviews()`. Reading the property yields the underlying
 * `Sequence<CollectedPreview>` directly, so consumers iterate the sequence with the
 * standard `kotlin.sequences` operators (`firstOrNull { ... }`, `take(N).toList()`,
 * `toList()`, …) without going through any wrapper accessor.
 *
 * The sequence is wrapped in a [Lazy] so that the per-`@Preview` factory lambdas the
 * compiler plugin emits are not invoked until the property is first read; iterating the
 * sequence then lazily constructs each [CollectedPreview] on demand. Consumers that only
 * need the first few entries (e.g. a search filter that returns 10 hits out of thousands
 * of `@Preview` declarations) avoid materializing the rest, dropping peak memory usage on
 * large preview corpora.
 *
 * Cross-module preview discovery does **not** rely on this type — it is handled by the
 * compiler plugin emitting per-`@Preview` hint functions
 * (`previewHint_<scope>(value: PreviewHintMarker_<hash>?): CollectedPreview` in the
 * `me.tbsten.compose.preview.lab.hints` package) that the consumer side resolves through
 * `IrPluginContext.referenceFunctions`.
 *
 * Users do not construct this class directly — it is produced exclusively by the compiler
 * plugin.
 *
 * Usage:
 * ```kotlin
 * // delegated property; type is `Sequence<CollectedPreview>`
 * val myPreviews by collectModulePreviews()
 *
 * // eager: drains the sequence to a list (used by Gallery, integration tests, etc.)
 * myPreviews.toList()
 *
 * // lazy: walk in order, stop early
 * myPreviews.firstOrNull { it.id == targetId }
 * myPreviews.take(10).toList()
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
     * Returns the underlying [Sequence], forcing the [Lazy] but **not** materializing
     * each [CollectedPreview]. Property delegation resolves through this method:
     * `val previews by collectModulePreviews()` causes the compiler to call
     * `getValue` on read, and the returned sequence iterates the per-`@Preview` factory
     * lambdas one element at a time — partial consumption
     * (`take(2)` / `firstOrNull { ... }`) leaves the remaining factories unevaluated.
     *
     * The same `Sequence` instance is returned on every property read (the [Lazy] caches
     * its first compute). Whether the returned `Sequence` itself can be iterated multiple
     * times is an implementation property of `Sequence` and is not guaranteed by this
     * type — call `toList()` once if you need a snapshot you can scan repeatedly.
     */
    public override operator fun getValue(thisRef: Any?, property: KProperty<*>): Sequence<CollectedPreview> = delegate.value
}
