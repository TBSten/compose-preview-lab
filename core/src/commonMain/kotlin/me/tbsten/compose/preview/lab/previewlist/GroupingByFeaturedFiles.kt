package me.tbsten.compose.preview.lab.previewlist

import me.tbsten.compose.preview.lab.CollectedPreview

internal fun List<CollectedPreview>.groupingByFeaturedFiles(
    featuredFiles: Map<String, List<String>>,
): Map<String, List<CollectedPreview>> {
    val previews = this
    return featuredFiles.entries.associate { (groupName, files) ->
        groupName to previews.filter { it.isInGroup(files) }
    }
}

private fun CollectedPreview.isInGroup(files: List<String>) = this.filePath in files
