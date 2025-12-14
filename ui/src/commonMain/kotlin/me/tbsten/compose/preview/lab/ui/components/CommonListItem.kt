package me.tbsten.compose.preview.lab.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme

private const val SelectedBorderWidth = 12

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
@InternalComposePreviewLabApi
fun CommonListItem(
    title: String,
    isSelected: Boolean,
    onSelect: (() -> Unit)? = null,
    isEnabled: Boolean = true,
    modifier: Modifier = Modifier,
) = CommonListItem(
    isSelected = isSelected,
    onSelect = onSelect,
    modifier = modifier,
    isEnabled = isEnabled,
    content = {
        Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth()) {
            Text(title, style = PreviewLabTheme.typography.body3)
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
@InternalComposePreviewLabApi
fun CommonListItem(
    title: String,
    isSelected: Boolean,
    onSelect: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    leadingContent: @Composable (() -> Unit)? = null,
) = CommonListItem(
    isSelected = isSelected,
    onSelect = onSelect,
    modifier = modifier,
    isEnabled = isEnabled,
    content = {
        leadingContent?.invoke()
        Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth()) {
            Text(title, style = PreviewLabTheme.typography.body3)
        }
    },
)

/**
 * Common list item with custom content
 *
 * The base list item component that other variants use internally.
 * Provides consistent styling and selection behavior with fully customizable content.
 *
 * @param isSelected Whether the item is currently selected
 * @param onSelect Callback invoked when the item is selected (null for non-selectable)
 * @param modifier Modifier to apply to the item
 * @param isEnabled Whether the item is enabled for interaction
 * @param content Composable content to display within the list item
 */
@Composable
@InternalComposePreviewLabApi
fun CommonListItem(
    isSelected: Boolean,
    onSelect: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    Button(
        variant = ButtonVariant.PrimaryGhost,
        shape = RectangleShape,
        onClick = onSelect ?: {},
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        isSelected = isSelected,
        isEnabled = isEnabled,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}
