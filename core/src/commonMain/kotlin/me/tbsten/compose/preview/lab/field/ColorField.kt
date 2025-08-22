package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import me.tbsten.compose.preview.lab.ui.components.Text

class ColorField(
    label: String,
    initialValue: Color,
) : MutablePreviewLabField<Color>(
    label = label,
    initialValue = initialValue,
) {
    @Composable
    override fun Content() {
        TextFieldContent(
            toString = {
                val hex = "00000000${it.toArgb().toUInt().toString(16)}".takeLast(8)
                "#$hex"
            },
            toValue = {
                runCatching {
                    val hexString = it.removePrefix("#")
                    val colorLong = hexString.toLong(16)
                    Color(colorLong)
                }
            },
        )
    }
}
