package me.tbsten.compose.preview.lab.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Button
import me.tbsten.compose.preview.lab.ui.components.ButtonVariant
import me.tbsten.compose.preview.lab.ui.components.Text

private const val SelectedBorderWidth = 12

@Composable
internal fun CommonListItem(
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

@Composable
internal fun CommonListItem(
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

@Composable
internal fun CommonListItem(
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
