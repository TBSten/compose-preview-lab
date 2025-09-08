package me.tbsten.compose.preview.lab.previewlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.takahirom.rin.rememberRetained
import me.tbsten.compose.preview.lab.CollectedPreview

@Composable
internal fun PreviewListTree(
    previews: List<CollectedPreview>,
    canAddToComparePanel: Boolean,
    isSelected: (CollectedPreview) -> Boolean,
    onSelect: (CollectedPreview) -> Unit,
    onAddToComparePanel: (CollectedPreview) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tree = rememberRetained(key = "$previews") {
        previews
            .toTree()
            .collapse()
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .padding(4.dp),
    ) {
        tree.forEach { node ->
            NodeView(
                previewTreeNode = node,
                isSelected = { it is PreviewTreeNode.Preview && isSelected(it.collectedPreview) },
                onSelect = { if (it is PreviewTreeNode.Preview) onSelect(it.collectedPreview) },
                onAddToComparePanel = { onAddToComparePanel(it.collectedPreview) },
                canAddToComparePanel = canAddToComparePanel,
            )
        }
    }
}

internal sealed interface PreviewTreeNode {
    val children: MutableList<PreviewTreeNode>

    data class Group(var groupName: String, override var children: MutableList<PreviewTreeNode>) : PreviewTreeNode

    data class Preview(val collectedPreview: CollectedPreview) : PreviewTreeNode {
        override val children: MutableList<PreviewTreeNode> = mutableListOf()
    }
}

internal typealias PreviewTree = MutableList<PreviewTreeNode>

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
internal fun List<CollectedPreview>.toTree(): PreviewTree {
    val tree = PreviewTreeNode.Group(
        groupName = "<root>",
        children = mutableListOf(),
    )
    for (preview in this) {
        val groupSegments =
            preview.displayName
                .split('.')
                .let { it.subList(0, it.lastIndex) }
        var currentNode: PreviewTreeNode.Group = tree
        for (segment in groupSegments) {
            val existingGroup =
                currentNode.children
                    .filterIsInstance<PreviewTreeNode.Group>()
                    .find { it.groupName == segment }
            if (existingGroup != null) {
                currentNode = existingGroup
            } else {
                val newGroup = PreviewTreeNode.Group(segment, mutableListOf())
                currentNode.children.add(newGroup)
                currentNode = newGroup
            }
        }
        currentNode.children.add(PreviewTreeNode.Preview(preview))
    }
    return tree.children
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
    fun PreviewTreeNode.process() {
        if (this is PreviewTreeNode.Group) {
            val firstChild = this.children.firstOrNull()
            if (this.children.size == 1 && firstChild is PreviewTreeNode.Group) {
                this.groupName = "${this.groupName}.${firstChild.groupName}"
                this.children = firstChild.children
            }

            children.forEach {
                it.process()
            }
        }
    }
    forEach { it.process() }
    return this
}
