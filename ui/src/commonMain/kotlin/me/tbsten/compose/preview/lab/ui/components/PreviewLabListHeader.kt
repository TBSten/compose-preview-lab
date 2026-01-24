package me.tbsten.compose.preview.lab.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme

@Composable
@UiComposePreviewLabApi
fun PreviewLabListHeader(title: String, actions: @Composable () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(PreviewLabTheme.colors.background)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(),
        ) {
            PreviewLabText(
                text = title,
                style = PreviewLabTheme.typography.label2,
                modifier = Modifier.weight(1f),
            )

            actions()
        }

        PreviewLabDivider()
    }
}
