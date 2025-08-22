package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable
import me.tbsten.compose.preview.lab.ui.components.Text

class ComposableField(
    label: String,
    initialValue: @Composable () -> Unit,
) : ImmutablePreviewLabField<@Composable () -> Unit>(
    label = label,
    initialValue = initialValue,
) {
    @Composable
    override fun Content() {
        Text("Composable Slot")
    }
}
