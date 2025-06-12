package me.tbsten.compose.preview.lab.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(horizontal = (level * 16).dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isExpanded) "▼" else "▶",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = group.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        if (isExpanded) {
            PreviewGroupList(
                items = group.children,
                selectedPreviewIndex = selectedPreviewIndex,
                onPreviewSelect = onPreviewSelect,
                level = level + 1
            )
        }
    }
}
