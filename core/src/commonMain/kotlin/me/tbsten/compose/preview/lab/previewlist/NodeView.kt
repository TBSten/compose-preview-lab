package me.tbsten.compose.preview.lab.previewlist

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.component.CommonIconButton
import me.tbsten.compose.preview.lab.component.CommonListItem
import me.tbsten.compose.preview.lab.component.CommonMenu
import me.tbsten.compose.preview.lab.core.generated.resources.Res
import me.tbsten.compose.preview.lab.core.generated.resources.icon_more_vert
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Button
import me.tbsten.compose.preview.lab.ui.components.ButtonVariant
import me.tbsten.compose.preview.lab.ui.components.Text
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun NodeView(
    previewTreeNode: PreviewTreeNode,
    canAddToComparePanel: Boolean,
    isSelected: (PreviewTreeNode) -> Boolean,
    onSelect: (PreviewTreeNode) -> Unit,
    onAddToComparePanel: (PreviewTreeNode.Preview) -> Unit,
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
                        onAddToComparePanel = onAddToComparePanel,
                        canAddToComparePanel = canAddToComparePanel,
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
            onAddToComparePanel = { onAddToComparePanel(previewTreeNode) },
            canAddToComparePanel = canAddToComparePanel,
            modifier = modifier,
        )
    }
}

@Composable
private fun GroupView(group: PreviewTreeNode.Group, children: @Composable () -> Unit, modifier: Modifier = Modifier) {
    var isOpen by remember { mutableStateOf(false) }

    Column {
        fun <T> toggleAnimationSpec() = tween<T>(durationMillis = 220)

        Button(
            variant = ButtonVariant.PrimaryGhost,
            onClick = { isOpen = !isOpen },
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = modifier.fillMaxWidth(),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "▶︎",
                    style = PreviewLabTheme.typography.body3,
                    modifier = Modifier
                        .rotate(
                            animateFloatAsState(
                                targetValue = if (isOpen) 90f else 0f,
                                animationSpec = toggleAnimationSpec(),
                            ).value,
                        ),
                )
                Text(text = group.groupName, style = PreviewLabTheme.typography.body3)
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
    canAddToComparePanel: Boolean,
    onSelect: () -> Unit,
    onAddToComparePanel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isMenuOpen by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Button(
            variant = ButtonVariant.PrimaryGhost,
            onClick = onSelect,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            isSelected = isSelected,
            modifier = modifier.weight(1f),
        ) {
            Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth()) {
                val previewName = preview.collectedPreview.displayName.split(".").last()
                Text(previewName, style = PreviewLabTheme.typography.body3)
            }
        }

        Column {
            CommonIconButton(
                painter = painterResource(Res.drawable.icon_more_vert),
                contentDescription = "Open menu",
                onClick = { isMenuOpen = true },
                modifier = Modifier.size(20.dp),
            )

            CommonMenu(
                expanded = isMenuOpen,
                onDismissRequest = { isMenuOpen = false },
            ) {
                CommonListItem(
                    title = "Open",
                    isSelected = false,
                    onSelect = onSelect,
                )

                CommonListItem(
                    title = "Add to compare panel",
                    isSelected = false,
                    onSelect = onAddToComparePanel,
                    isEnabled = canAddToComparePanel,
                )
            }
        }
    }
}
