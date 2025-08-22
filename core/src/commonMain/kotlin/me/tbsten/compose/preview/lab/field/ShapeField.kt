package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import me.tbsten.compose.preview.lab.ui.components.Text

class ShapeField(
    label: String,
    initialValue: Shape,
) : ImmutablePreviewLabField<Shape>(
    label = label,
    initialValue = initialValue,
) {
    @Composable
    override fun Content() {
        Text(value::class.simpleName ?: value.toString())
    }
}
