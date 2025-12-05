package me.tbsten.compose.preview.lab.field

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.component.animated
import me.tbsten.compose.preview.lab.ui.Gray50
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Surface
import me.tbsten.compose.preview.lab.ui.components.Text

/**
 * Field that holds a Boolean value.
 * Switch allows you to switch values.
 *
 * # Usage
 *
 * ```kt
 * // Basic usage for component enabled state
 * @Preview
 * @Composable
 * fun ButtonPreview() = PreviewLab {
 *     val enabled: Boolean = fieldValue { BooleanField("enabled", true) }
 *     MyButton(
 *         text = "Click me",
 *         enabled = enabled
 *     )
 * }
 * ```
 *
 * @param label label of the field.
 * @param initialValue initial value of the field.
 * @see MutablePreviewLabField
 */
open class BooleanField(label: String, initialValue: Boolean) :
    MutablePreviewLabField<Boolean>(
        label = label,
        initialValue = initialValue,
    ) {
    override fun testValues(): List<Boolean> = super.testValues() + listOf(true, false)
    override fun valueCode(): String = "$value"

    @Composable
    override fun Content() {
        BooleanSwitch(
            boolean = value,
            onChange = { value = it },
        )
    }
}

@Composable
private fun BooleanSwitch(boolean: Boolean, onChange: (Boolean) -> Unit) {
    Surface(
        shape = RoundedCornerShape(percent = 50),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        ) {
            listOf(true, false).forEach {
                val isSelected = boolean == it

                Surface(
                    color = (if (isSelected) Gray50 else Color.Transparent).animated(),
                    shape = RoundedCornerShape(percent = 50),
                    onClick = { onChange(it) },
                ) {
                    Text(
                        text = it.toString(),
                        style = PreviewLabTheme.typography.label1,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}
