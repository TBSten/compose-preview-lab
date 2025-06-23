package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Switch
import me.tbsten.compose.preview.lab.ui.components.Text

/**
 * Field that holds a Boolean value.
 * Switch allows you to switch values.
 *
 * ```kt
 * PreviewLab {
 *   MyButton(
 *     ...,
 *     enabled = field { BooleanField("enabled", true) },
 *   )
 * }
 * ```
 *
 * @param label label of the field.
 * @param initialValue initial value of the field.
 *
 * @see MutablePreviewLabField
 */
open class BooleanField(label: String, initialValue: Boolean) :
    MutablePreviewLabField<Boolean>(
        label = label,
        initialValue = initialValue,
    ) {
    @Composable
    override fun Content() {
        Switch(
            checked = value,
            onCheckedChange = { value = it },
            thumbContent = {
                Text(text = value.toString(), style = PreviewLabTheme.typography.label3)
            },
        )
    }
}
