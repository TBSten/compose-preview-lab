/*
 * Copyright (c) 2026 Composable Horizons — MIT License (see ui/THIRD_PARTY_NOTICES.md).
 * Adapted for Compose Preview Lab: PreviewLab* prefix, compose-unstyled 2.6.0, PreviewLabTheme facade colours.
 * The look of a Composables UI `DropdownMenuItem`, rebuilt directly on `UnstyledButton` (PreviewLab lists and
 * menus are plain Popups, not an `UnstyledDropdownMenu`).
 */
package me.tbsten.compose.preview.lab.ui.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composeunstyled.ProvideContentColor
import com.composeunstyled.UnstyledButton
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.util.thenIf

/**
 * Common list item with title display
 *
 * Displays a selectable list item with a title text. Provides consistent
 * styling and behavior across the application for simple text-based list items.
 *
 * @param title Text to display in the list item
 * @param isSelected Whether the item is currently selected
 * @param onSelect Callback invoked when the item is selected (null for non-selectable)
 * @param isEnabled Whether the item is enabled for interaction
 * @param modifier Modifier to apply to the item
 */
@Composable
@UiComposePreviewLabApi
fun PreviewLabListItem(
    title: String,
    isSelected: Boolean,
    onSelect: (() -> Unit)? = null,
    isEnabled: Boolean = true,
    modifier: Modifier = Modifier,
) = PreviewLabListItem(
    isSelected = isSelected,
    onSelect = onSelect,
    modifier = modifier,
    isEnabled = isEnabled,
    content = {
        Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth()) {
            PreviewLabText(title, style = PreviewLabTheme.typography.body2)
        }
    },
)

/**
 * Common list item with title and optional leading content
 *
 * Displays a selectable list item with title text and optional leading content.
 * The leading content appears before the title text, useful for icons or other decorative elements.
 *
 * @param title Text to display in the list item
 * @param isSelected Whether the item is currently selected
 * @param onSelect Callback invoked when the item is selected (null for non-selectable)
 * @param modifier Modifier to apply to the item
 * @param isEnabled Whether the item is enabled for interaction
 * @param leadingContent Optional composable content to display before the title
 */
@Composable
@UiComposePreviewLabApi
fun PreviewLabListItem(
    title: String,
    isSelected: Boolean,
    onSelect: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    leadingContent: @Composable (() -> Unit)? = null,
) = PreviewLabListItem(
    isSelected = isSelected,
    onSelect = onSelect,
    modifier = modifier,
    isEnabled = isEnabled,
    content = {
        leadingContent?.invoke()
        Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth()) {
            PreviewLabText(title, style = PreviewLabTheme.typography.body2)
        }
    },
)

/**
 * Common list item with custom content
 *
 * The base list item component that other variants use internally. Looks like a Composables UI menu item:
 * 12dp corners, 36dp minimum height, 12/6dp padding, `secondary` background while hovered / focused / pressed,
 * `selectedControl` background when [isSelected], 33 % alpha when disabled. The content is laid out in a
 * start-aligned, full-width row so titles stay left-aligned.
 *
 * @param isSelected Whether the item is currently selected
 * @param onSelect Callback invoked when the item is selected (null for non-selectable)
 * @param modifier Modifier to apply to the item
 * @param isEnabled Whether the item is enabled for interaction
 * @param content Composable content to display within the list item
 */
@Composable
@UiComposePreviewLabApi
fun PreviewLabListItem(
    isSelected: Boolean,
    onSelect: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = PreviewLabTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val pressed by interactionSource.collectIsPressedAsState()
    val active = isEnabled && (hovered || focused || pressed)
    val itemShape = RoundedCornerShape(ListItemCornerRadius)
    val background = when {
        isSelected -> colors.selectedControl // selected row (SelectButton current choice, NodeView)
        active -> colors.secondary // Composables DropdownMenuItem hover / focus / press fill
        else -> Color.Transparent
    }
    UnstyledButton(
        onClick = onSelect ?: {},
        enabled = isEnabled,
        contentPadding = ListItemContentPadding,
        interactionSource = interactionSource,
        indication = LocalIndication.current,
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .fillMaxWidth()
            .previewLabFocusRing(interactionSource = interactionSource, shape = itemShape)
            .clip(itemShape)
            .background(background, itemShape)
            .thenIf(!isEnabled) { alpha(ListItemDisabledAlpha) }
            .heightIn(min = ListItemMinHeight),
    ) {
        ProvideContentColor(if (isSelected) colors.onSelectedControl else colors.onPanel) {
            // Keep the start-aligned, full-width row — menu item titles must not be centred.
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                content()
            }
        }
    }
}

private val ListItemCornerRadius = 12.dp
private val ListItemMinHeight = 36.dp
private val ListItemContentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)

/** Composables UI `disabledAlpha` token value (see Theme.kt `properties[alphas]`). */
private const val ListItemDisabledAlpha = 0.33f
