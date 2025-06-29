package me.tbsten.compose.preview.lab.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.layout.LayoutNodeId
import me.tbsten.compose.preview.lab.layout.PreviewLabLayoutNode
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Text

@Composable
internal fun LayoutSection(
    contentRootOffset: DpOffset?,
    layoutNodes: List<PreviewLabLayoutNode>,
    selectedLayoutNodeIds: Set<LayoutNodeId>,
    hoveredLayoutNodeIds: Set<LayoutNodeId>,
    onNodeClick: (LayoutNodeId) -> Unit,
) {
    val resolvedNodes = layoutNodes
        .filterIsInstance<PreviewLabLayoutNode.Resolved>()

    LazyColumn {
        stickyHeader {
            CommonListHeader(
                title = "${resolvedNodes.size} items (${layoutNodes.size})",
                actions = {},
            )
        }

        items(resolvedNodes) { layoutNode ->
            val isSelected = selectedLayoutNodeIds.any { it == layoutNode.id }
            val isHovered = hoveredLayoutNodeIds.any { it == layoutNode.id }

            CommonListItem(
                isSelected = isSelected,
                onSelect = { onNodeClick(layoutNode.id) },
            ) {
                Column {
                    Text(
                        text = layoutNode.label,
                        fontWeight = FontWeight.Bold,
                        style = PreviewLabTheme.typography.body2,
                    )

                    Column(
                        modifier = Modifier.padding(
                            start = 16.dp,
                        ),
                    ) {
                        if (contentRootOffset != null) {
                            Text(
                                text = layoutNode.offsetInAppRoot
                                    .let { it.x - contentRootOffset.x to it.y - contentRootOffset.y }
                                    .let { (x, y) -> "offset: $x, $y" },
                                style = PreviewLabTheme.typography.body3,
                            )
                        }
                        Text(
                            text = layoutNode.size.let { "size: ${it.width} × ${it.height}" },
                            style = PreviewLabTheme.typography.body3,
                        )
                        if (isHovered) {
                            Text("isHovered")
                        }
                    }
                }
            }
        }
    }
}
