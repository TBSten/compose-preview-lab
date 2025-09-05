package me.tbsten.compose.preview.lab.component.colorpicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * refs: https://github.com/krizzu/kolor-picker
 */
@Composable
internal fun CommonColorPicker(color: Color, onColorSelected: (Color) -> Unit, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier,
    ) {
        HSVPicker(
            selectedColor = color,
            onColorSelected = onColorSelected,
            modifier = Modifier.weight(8f),
        )

        HueSlider(
            selectedColor = color,
            onColorSelected = { onColorSelected(it) },
            modifier = Modifier.weight(1f),
        )

        AlphaSlider(
            selectedColor = color,
            onColorSelected = { onColorSelected(it) },
            modifier = Modifier.weight(1f),
        )
    }
}
