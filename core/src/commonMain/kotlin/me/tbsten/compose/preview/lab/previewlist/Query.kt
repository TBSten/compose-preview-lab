package me.tbsten.compose.preview.lab.previewlist

import me.tbsten.compose.preview.lab.CollectedPreview

internal fun List<CollectedPreview>.filterByQuery(query: String): List<CollectedPreview> {
    if (query.isBlank()) return this

    val lowerCaseQuery = query.lowercase()
    val queries = lowerCaseQuery.split(Regex("\\s+")).filter { it.isNotBlank() }
    return filter { preview ->
        queries.any { preview.displayName.lowercase().contains(it) }
    }
}
