package me.tbsten.compose.preview.lab.field

import me.tbsten.compose.preview.lab.MutablePreviewLabField

fun MutablePreviewLabField<String>.withImageUrlHint(includeDummyImages: Boolean = true, includeInvalidImages: Boolean = true) =
    withHint(
        choices = buildMap {
            if (includeDummyImages) {
                put("Png", "https://dummyimage.com/600x300/ff0000/ffffff.png")
                put("Gif", "https://dummyimage.com/600x300/ff0000/ffffff.gif")
            }
            if (includeInvalidImages) {
                put("Invalid (404)", "https://example.com/invalid.png")
                put("Invalid (200, not image)", "https://example.com")
            }
        }.toPairArray(),
    )

private fun <K, V> Map<K, V>.toPairArray() = entries.map { it.key to it.value }.toList().toTypedArray()
