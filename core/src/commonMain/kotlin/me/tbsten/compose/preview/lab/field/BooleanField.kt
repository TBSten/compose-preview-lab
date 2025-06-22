package me.tbsten.compose.preview.lab.field

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable

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
        )
    }
}
