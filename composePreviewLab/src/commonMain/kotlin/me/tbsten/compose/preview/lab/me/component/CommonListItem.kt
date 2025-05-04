package me.tbsten.compose.preview.lab.me.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.me.util.thenIf
import me.tbsten.compose.preview.lab.me.util.thenIfNotNull

private const val selectedBorderWidth = 12

@Composable
internal fun CommonListItem(
    title: String,
    isSelected: Boolean,
    onSelect: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) = CommonListItem(
    isSelected = isSelected,
    onSelect = onSelect,
    modifier = modifier,
    content = { Text(title, style = MaterialTheme.typography.bodyMedium) },
)

@Composable
internal fun CommonListItem(
    isSelected: Boolean,
    onSelect: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .thenIfNotNull(onSelect) { clickable(onClick = it) }
            .thenIf(isSelected) {
                drawWithContent {
                    drawRect(color = Color.LightGray)
                    drawContent()
                    drawLine(
                        color = Color.Gray,
                        strokeWidth = selectedBorderWidth.dp.toPx(),
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                    )
                }.padding(start = selectedBorderWidth.dp)
            }
            .padding(vertical = 16.dp, horizontal = 8.dp)
            .fillMaxWidth()
    ) {
        content()
    }
}
