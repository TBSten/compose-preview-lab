package me.tbsten.compose.preview.lab.gallery.previewlist

import me.tbsten.compose.preview.lab.PreviewLabPreview

internal fun List<PreviewLabPreview>.filterByQuery(query: String): List<PreviewLabPreview> {
    if (query.isBlank()) return this

    val lowerCaseQuery = query.lowercase()
    val queries = lowerCaseQuery.split(Regex("\\s+")).filter { it.isNotBlank() }
    return filter { preview ->
        queries.any { preview.displayName.lowercase().contains(it) }
    }
}
