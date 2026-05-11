package me.tbsten.compose.preview.lab.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme

private const val SelectedBorderWidth = 12

/**
 * Selectable list item with a single title. Pass `onSelect = null` to make the row
 * non-selectable.
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
            PreviewLabText(title, style = PreviewLabTheme.typography.body3)
        }
    },
)

/**
 * Selectable list item with a title and optional [leadingContent] (e.g. an icon) before it.
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
            PreviewLabText(title, style = PreviewLabTheme.typography.body3)
        }
    },
)

/**
 * Base list-item used by the title / leading-content overloads. Apply when neither variant's
 * layout is enough and fully custom [content] is needed.
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
    PreviewLabButton(
        variant = PreviewLabButtonVariant.PrimaryGhost,
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
