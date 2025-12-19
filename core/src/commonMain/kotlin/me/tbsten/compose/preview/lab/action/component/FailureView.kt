package me.tbsten.compose.preview.lab.action.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Text

@Composable
internal fun FailureView(error: Throwable, showStacktrace: Boolean = false) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(8.dp),
    ) {
        Text(
            text = "${error::class.simpleName}: ${error.message}",
            color = PreviewLabTheme.colors.error,
            style = PreviewLabTheme.typography.body2,
            fontWeight = FontWeight.Bold,
        )

        if (showStacktrace) {
            Text(
                text = error.stackTraceToString(),
                color = PreviewLabTheme.colors.error.copy(alpha = 0.8f),
                style = PreviewLabTheme.typography.body3,
            )
        }
    }
}
