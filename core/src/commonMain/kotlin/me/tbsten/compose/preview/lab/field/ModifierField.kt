package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.tbsten.compose.preview.lab.ui.components.Text

class ModifierField(
    label: String,
    initialValue: Modifier,
) : ImmutablePreviewLabField<Modifier>(
    label = label,
    initialValue = initialValue,
) {
    @Composable
    override fun Content() {
        Text(value.toString())
    }
}
