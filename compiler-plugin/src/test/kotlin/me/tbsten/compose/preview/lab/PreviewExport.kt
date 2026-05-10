package me.tbsten.compose.preview.lab

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Test stub for PreviewExport. Mirrors the real class (in `core`): a property delegate
 * that yields `Sequence<CollectedPreview>` directly via [getValue]. kctfork tests do not
 * depend on the real `core` module, so the `@InternalComposePreviewLabApi` opt-in marker
 * on the constructor is omitted.
 */
class PreviewExport(private val delegate: Lazy<Sequence<CollectedPreview>>) :
    ReadOnlyProperty<Any?, Sequence<CollectedPreview>> {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): Sequence<CollectedPreview> = delegate.value
}
