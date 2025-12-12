package me.tbsten.compose.preview.lab.previewlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.PreviewLabPreview
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun PreviewListTree(
    previews: List<PreviewLabPreview>,
    canAddToComparePanel: Boolean,
    isSelected: (PreviewLabPreview) -> Boolean,
    onSelect: (PreviewLabPreview) -> Unit,
    onAddToComparePanel: (PreviewLabPreview) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tree = remember(key1 = "$previews") {
        previews.toPreviewTree()
    }

    PreviewTreeContent(
        tree = tree,
        canAddToComparePanel = canAddToComparePanel,
        isSelected = isSelected,
        onSelect = onSelect,
        onAddToComparePanel = onAddToComparePanel,
        modifier = modifier,
    )
}

@Composable
private fun PreviewTreeContent(
    tree: PreviewTree,
    canAddToComparePanel: Boolean,
    isSelected: (PreviewLabPreview) -> Boolean,
    onSelect: (PreviewLabPreview) -> Unit,
    onAddToComparePanel: (PreviewLabPreview) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.padding(4.dp),
    ) {
        tree.forEach { node ->
            PreviewTreeNodeView(
                node = node,
                canAddToComparePanel = canAddToComparePanel,
                isSelected = isSelected,
                onSelect = onSelect,
                onAddToComparePanel = onAddToComparePanel,
            )
        }
    }
}

@Composable
private fun PreviewTreeNodeView(
    node: PreviewTreeNode,
    canAddToComparePanel: Boolean,
    isSelected: (PreviewLabPreview) -> Boolean,
    onSelect: (PreviewLabPreview) -> Unit,
    onAddToComparePanel: (PreviewLabPreview) -> Unit,
    modifier: Modifier = Modifier,
) {
    NodeView(
        previewTreeNode = node,
        isSelected = { it is PreviewTreeNode.Preview && isSelected(it.preview) },
        onSelect = { if (it is PreviewTreeNode.Preview) onSelect(it.preview) },
        onAddToComparePanel = { onAddToComparePanel(it.preview) },
        canAddToComparePanel = canAddToComparePanel,
        modifier = modifier,
    )
}

// region Data Models

internal sealed interface PreviewTreeNode {
    val children: MutableList<PreviewTreeNode>

    data class Group(var groupName: String, override var children: MutableList<PreviewTreeNode>) : PreviewTreeNode

    data class Preview(val preview: PreviewLabPreview) : PreviewTreeNode {
        override val children: MutableList<PreviewTreeNode> = mutableListOf()
    }
}

internal typealias PreviewTree = MutableList<PreviewTreeNode>

// endregion

// region Tree Building Logic

/**
 * Converts a list of previews to a tree structure and collapses single-child groups.
 */
private fun List<PreviewLabPreview>.toPreviewTree(): PreviewTree = toTree().collapse()

/**
 * ```
 * a.b.c.D
 * a.b.c.D.E
 * a.b.C
 * a.b.e.F
 * F
 *
 * ↓
 *
 * a
 * |- b
 * |- |- c
 * |- |- |- D
 * |- |- |- D
 * |- |- |- |- E
 * |- |- C
 * |- |- e
 * |- |- |- F
 * F
 * ```
 */
internal fun List<PreviewLabPreview>.toTree(): PreviewTree {
    val tree = PreviewTreeNode.Group(
        groupName = "<root>",
        children = mutableListOf(),
    )
    for (preview in this) {
        val groupSegments = preview.displayName.extractGroupSegments()
        var currentNode: PreviewTreeNode.Group = tree
        for (segment in groupSegments) {
            currentNode = currentNode.findOrCreateChildGroup(segment)
        }
        currentNode.children.add(PreviewTreeNode.Preview(preview))
    }
    return tree.children
}

private fun String.extractGroupSegments(): List<String> = split('.').let { it.subList(0, it.lastIndex) }

private fun PreviewTreeNode.Group.findOrCreateChildGroup(segmentName: String): PreviewTreeNode.Group {
    val existingGroup = children
        .filterIsInstance<PreviewTreeNode.Group>()
        .find { it.groupName == segmentName }
    if (existingGroup != null) {
        return existingGroup
    }
    val newGroup = PreviewTreeNode.Group(segmentName, mutableListOf())
    children.add(newGroup)
    return newGroup
}

/**
 * ```
 * a
 * |- b
 * |- |- c
 * |- |- |- D
 * |- |- |- D
 * |- |- |- |- E
 * |- |- C
 * |- |- e
 * |- |- |- F
 * F
 *
 * ↓
 *
 * a.b
 * |- c
 * |- |- D
 * |- |- D
 * |- |- |- E
 * |- C
 * |- |e
 * |- |- F
 * F
 * ```
 */
internal fun PreviewTree.collapse(): PreviewTree {
    forEach { it.collapseNode() }
    return this
}

private fun PreviewTreeNode.collapseNode() {
    if (this is PreviewTreeNode.Group) {
        collapseSingleChildGroup()
        children.forEach { it.collapseNode() }
    }
}

private fun PreviewTreeNode.Group.collapseSingleChildGroup() {
    val firstChild = children.firstOrNull()
    if (children.size == 1 && firstChild is PreviewTreeNode.Group) {
        groupName = "$groupName.${firstChild.groupName}"
        children = firstChild.children
    }
}

// endregion

// region Previews

@Preview
@Composable
private fun PreviewListTreePreview() {
    val previews = createSamplePreviews()
    PreviewListTree(
        previews = previews,
        canAddToComparePanel = true,
        isSelected = { false },
        onSelect = {},
        onAddToComparePanel = {},
    )
}

private fun createSamplePreviews(): List<PreviewLabPreview> = listOf(
    PreviewLabPreview(id = "a.b.c", displayName = "a.b.c") {},
    PreviewLabPreview(id = "a.b.d.x", displayName = "a.b.d.x") {},
    PreviewLabPreview(id = "a.b.e", displayName = "a.b.e") {},
    PreviewLabPreview(id = "a.b", displayName = "a.b") {},
    PreviewLabPreview(id = "e.f", displayName = "e.f") {},
    PreviewLabPreview(id = "e", displayName = "e") {},
    PreviewLabPreview(id = "e.a.b", displayName = "e.a.b") {},
    PreviewLabPreview(id = "1.2.3.4.5", displayName = "1.2.3.4.5") {},
    PreviewLabPreview(id = "1.2.3.4.6", displayName = "1.2.3.4.6") {},
)

// endregion
