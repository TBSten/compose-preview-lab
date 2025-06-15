package me.tbsten.compose.preview.lab.previewlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
internal fun NodeView(
    previewTreeNode: PreviewTreeNode,
    isSelected: (PreviewTreeNode) -> Boolean,
    onSelect: (PreviewTreeNode) -> Unit,
    modifier: Modifier = Modifier,
): Unit = when (previewTreeNode) {
    is PreviewTreeNode.Group -> {
        GroupView(
            group = previewTreeNode,
            children = {
                previewTreeNode.children.forEach { child ->
                    NodeView(
                        previewTreeNode = child,
                        isSelected = isSelected,
                        onSelect = onSelect,
                    )
                }
            },
            modifier = modifier,
        )
    }

    is PreviewTreeNode.Preview -> {
        PreviewView(
            preview = previewTreeNode,
            isSelected = isSelected(previewTreeNode),
            onSelect = { onSelect(previewTreeNode) },
            modifier = modifier,
        )
    }
}


@Composable
private fun GroupView(
    group: PreviewTreeNode.Group,
    children: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isOpen by remember { mutableStateOf(false) }

    Column {
        fun <T> toggleAnimationSpec() = tween<T>(durationMillis = 220)

        TextButton(
            onClick = { isOpen = !isOpen },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = modifier.fillMaxWidth(),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "▶︎",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .rotate(
                            animateFloatAsState(
                                targetValue = if (isOpen) 90f else 0f,
                                animationSpec = toggleAnimationSpec(),
                            ).value
                        )
                )
                Text(text = group.groupName, style = MaterialTheme.typography.bodySmall)
            }
        }

        AnimatedVisibility(
            visible = isOpen,
            enter = fadeIn(toggleAnimationSpec()) + expandVertically(toggleAnimationSpec()),
            exit = fadeOut(toggleAnimationSpec()) + shrinkVertically(toggleAnimationSpec()),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(start = 12.dp)) {
                children()
            }
        }
    }
}

@Composable
private fun PreviewView(
    preview: PreviewTreeNode.Preview,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val buttonColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)

    TextButton(
        onClick = onSelect,
        colors = ButtonDefaults.textButtonColors(
            containerColor = buttonColor,
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth()) {
            val previewName = preview.collectedPreview.displayName.split(".").last()
            Text(previewName, style = MaterialTheme.typography.bodySmall)
        }
    }
}
