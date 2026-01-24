package me.tbsten.compose.preview.lab.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.LocalContentColor
import me.tbsten.compose.preview.lab.ui.LocalTextStyle
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.util.thenIf

@Composable
@UiComposePreviewLabApi
fun PreviewLabTabRow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column {
        Row(
            modifier = modifier
                .horizontalScroll(rememberScrollState()),
        ) {
            content()
        }
        PreviewLabDivider()
    }
}

@Composable
@UiComposePreviewLabApi
fun PreviewLabTab(selected: Boolean, onClick: () -> Unit, text: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .thenIf(selected) {
                val color = PreviewLabTheme.colors.primary
                drawWithContent {
                    drawContent()
                    drawRect(
                        color = color,
                        size = size.copy(height = 2.dp.toPx()),
                        topLeft = Offset(0f, size.height - 2.dp.toPx()),
                    )
                }
            }
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides PreviewLabTheme.typography.body2,
            LocalContentColor provides if (selected) PreviewLabTheme.colors.primary else LocalContentColor.current,
        ) {
            text()
        }
    }
}
