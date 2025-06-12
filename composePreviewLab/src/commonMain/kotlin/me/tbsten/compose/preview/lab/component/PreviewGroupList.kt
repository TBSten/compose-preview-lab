package me.tbsten.compose.preview.lab.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.PreviewGroup
import me.tbsten.compose.preview.lab.PreviewGroupItem

@Composable
fun PreviewGroupList(
    items: List<PreviewGroupItem>,
    selectedPreviewIndex: Int,
    onPreviewSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    level: Int = 0
) {
    if (level == 0 && items.size > 10) {
        LazyColumn(modifier = modifier) {
            items(items) { item ->
                when (item) {
                    is PreviewGroupItem.Group -> {
                        PreviewGroupHeader(
                            group = item.group,
                            level = level,
                            onPreviewSelect = onPreviewSelect,
                            selectedPreviewIndex = selectedPreviewIndex
                        )
                    }

                    is PreviewGroupItem.Preview -> {
                        CommonListItem(
                            title = item.preview.displayName,
                            isSelected = item.index == selectedPreviewIndex,
                            onSelect = { onPreviewSelect(item.index) },
                            modifier = Modifier.padding(start = (level * 16).dp)
                        )
                    }
                }
            }
        }
    } else {
        Column(modifier = modifier) {
            items.forEach { item ->
                when (item) {
                    is PreviewGroupItem.Group -> {
                        PreviewGroupHeader(
                            group = item.group,
                            level = level,
                            onPreviewSelect = onPreviewSelect,
                            selectedPreviewIndex = selectedPreviewIndex
                        )
                    }

                    is PreviewGroupItem.Preview -> {
                        CommonListItem(
                            title = item.preview.displayName,
                            isSelected = item.index == selectedPreviewIndex,
                            onSelect = { onPreviewSelect(item.index) },
                            modifier = Modifier.padding(start = (level * 16).dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewGroupHeader(
    group: PreviewGroup,
    level: Int,
    selectedPreviewIndex: Int,
    onPreviewSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(group.isExpanded) }

    Column(modifier = modifier) {
        CommonListItem(
            title = group.name,
            isSelected = false,
            onSelect = { isExpanded = !isExpanded },
            modifier = Modifier
                .padding(start = (level * 16).dp)
                .semantics {
                    contentDescription =
                        if (isExpanded) "Expanded: ${group.name}" else "Collapsed: ${group.name}"
                },
            leadingContent = {
                Text(
                    text = "▼",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .rotate(animateFloatAsState(if (isExpanded) 0f else -90f).value)
                        .semantics {
                            contentDescription = if (isExpanded) "Expanded" else "Collapsed"
                        }
                )
            }
        )

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(tween(200, easing = EaseOutExpo)),
            exit = shrinkVertically(tween(200, easing = EaseOutExpo))
        ) {
            PreviewGroupList(
                items = group.children,
                selectedPreviewIndex = selectedPreviewIndex,
                onPreviewSelect = onPreviewSelect,
                level = level + 1
            )
        }
    }
}
