package me.tbsten.compose.preview.lab.action.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.PreviewLabText

@Composable
internal fun NoResultsView(label: String) {
    Column(Modifier.padding(8.dp)) {
        PreviewLabText(
            text = "No $label execution. Please run $label in `Actions` InspectorTab.",
            style = PreviewLabTheme.typography.body2,
            color = PreviewLabTheme.colors.textSecondary,
        )
    }
}
