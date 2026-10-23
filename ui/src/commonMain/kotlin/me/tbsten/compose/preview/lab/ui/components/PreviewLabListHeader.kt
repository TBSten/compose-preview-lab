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

/**
 * Header row for a list section: a muted `label2` title, trailing [actions], and a separator underneath.
 * Painted with the `panel` colour (chrome panes sit on the `background` canvas).
 *
 * @param title Section title
 * @param actions Trailing content (typically icon buttons)
 * @param modifier Modifier to apply to the header
 */
@Composable
@UiComposePreviewLabApi
fun PreviewLabListHeader(title: String, actions: @Composable () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(PreviewLabTheme.colors.panel)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(),
        ) {
            PreviewLabText(
                text = title,
                style = PreviewLabTheme.typography.label2,
                color = PreviewLabTheme.colors.muted,
                modifier = Modifier.weight(1f),
            )

            actions()
        }

        PreviewLabHorizontalSeparator()
    }
}
