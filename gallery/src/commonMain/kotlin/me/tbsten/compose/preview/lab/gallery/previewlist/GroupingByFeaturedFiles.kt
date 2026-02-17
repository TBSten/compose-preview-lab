package me.tbsten.compose.preview.lab.gallery.previewlist

import me.tbsten.compose.preview.lab.PreviewLabPreview

internal fun List<PreviewLabPreview>.groupingByFeaturedFiles(
    featuredFiles: Map<String, List<String>>,
): Map<String, List<PreviewLabPreview>> {
    val previews = this
    return featuredFiles.entries.associate { (groupName, files) ->
        groupName to previews.filter { it.isInGroup(files) }
    }
}

private fun PreviewLabPreview.isInGroup(patterns: List<String>): Boolean {
    val filePath = this.filePath ?: return false
    return patterns.any { pattern -> matchesGlob(filePath, pattern) }
}
