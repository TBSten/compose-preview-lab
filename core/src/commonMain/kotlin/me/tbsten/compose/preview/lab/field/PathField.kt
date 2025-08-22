package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Path
import me.tbsten.compose.preview.lab.ui.components.Text

class PathField(
    label: String,
    initialValue: Path,
) : ImmutablePreviewLabField<Path>(
    label = label,
    initialValue = initialValue,
) {
    @Composable
    override fun Content() {
        Text("Path(isEmpty=${value.isEmpty})")
    }
}
