package me.tbsten.compose.preview.lab

data class PreviewGroup(
    val name: String,
    val children: List<PreviewGroupItem> = emptyList(),
    val isExpanded: Boolean = true
)

sealed class PreviewGroupItem {
    data class Group(val group: PreviewGroup) : PreviewGroupItem()
    data class Preview(val preview: CollectedPreview, val index: Int) : PreviewGroupItem()
}

fun List<CollectedPreview>.groupByDisplayName(): List<PreviewGroupItem> {
    val rootGroups = mutableMapOf<String, MutableList<PreviewGroupItem>>()
    
    this.forEachIndexed { index, preview ->
        val segments = preview.displayName.split(".")
        
        if (segments.size == 1) {
            val groupName = "Root"
            rootGroups.getOrPut(groupName) { mutableListOf() }
                .add(PreviewGroupItem.Preview(preview, index))
        } else {
            buildHierarchy(segments, preview, index, rootGroups)
        }
    }
    
    val result = mutableListOf<PreviewGroupItem>()
    rootGroups.forEach { (groupName, items) ->
        if (groupName == "Root" && rootGroups.size == 1) {
            result.addAll(items)
        } else {
            result.add(PreviewGroupItem.Group(PreviewGroup(groupName, items)))
        }
    }
    return result
}

private fun buildHierarchy(
    segments: List<String>,
    preview: CollectedPreview,
    index: Int,
    rootGroups: MutableMap<String, MutableList<PreviewGroupItem>>
) {
    if (segments.isEmpty()) return
    
    val currentSegment = segments.first()
    val remainingSegments = segments.drop(1)
    
    val currentGroup = rootGroups.getOrPut(currentSegment) { mutableListOf() }
    
    if (remainingSegments.isEmpty()) {
        currentGroup.add(PreviewGroupItem.Preview(preview, index))
    } else {
        val existingSubGroup = currentGroup.find { item ->
            item is PreviewGroupItem.Group && item.group.name == remainingSegments.first()
        } as? PreviewGroupItem.Group
        
        if (existingSubGroup == null) {
            val subGroups = mutableMapOf<String, MutableList<PreviewGroupItem>>()
            buildHierarchy(remainingSegments, preview, index, subGroups)
            
            subGroups.forEach { (subGroupName, subItems) ->
                currentGroup.add(PreviewGroupItem.Group(PreviewGroup(subGroupName, subItems)))
            }
        } else {
            val updatedSubGroup = existingSubGroup.group
            val subGroups = mutableMapOf<String, MutableList<PreviewGroupItem>>()
            subGroups[updatedSubGroup.name] = updatedSubGroup.children.toMutableList()
            buildHierarchy(remainingSegments, preview, index, subGroups)
            
            val updatedIndex = currentGroup.indexOf(existingSubGroup)
            currentGroup[updatedIndex] = PreviewGroupItem.Group(
                updatedSubGroup.copy(children = subGroups[updatedSubGroup.name] ?: emptyList())
            )
        }
    }
}
