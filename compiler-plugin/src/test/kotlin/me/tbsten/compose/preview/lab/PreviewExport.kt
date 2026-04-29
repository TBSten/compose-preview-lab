package me.tbsten.compose.preview.lab

import kotlin.reflect.KProperty

/**
 * Test stub for PreviewExport. Mirrors the real class (in `core`) shape but without the
 * `@InternalComposePreviewLabApi` opt-in marker on the constructor (kctfork tests do not
 * depend on the real `core` module).
 */
class PreviewExport(private val delegate: Lazy<List<CollectedPreview>>) {
    val value: List<CollectedPreview> get() = delegate.value
    operator fun getValue(thisRef: Any?, property: KProperty<*>): List<CollectedPreview> = delegate.value
}
