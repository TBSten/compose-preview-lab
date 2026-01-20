package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import me.tbsten.compose.preview.lab.MutablePreviewLabField
import me.tbsten.compose.preview.lab.ui.components.Checkbox

/**
 * A field for editing Boolean values with a checkbox UI.
 *
 * # Usage
 *
 * ```kt
 * @Preview
 * @Composable
 * fun MyComponentPreview() = PreviewLab {
 *     val checked: Boolean = fieldValue { BooleanField("Checked", false) }
 *
 *     MyComponent(checked = checked)
 * }
 * ```
 *
 * @param label Label displayed in the UI for this field.
 * @param initialValue Initial boolean value for this field.
 *
 * @see me.tbsten.compose.preview.lab.PreviewLabField
 * @see me.tbsten.compose.preview.lab.PreviewLabScope.fieldValue
 */
public open class BooleanField(label: String, initialValue: Boolean) :
    MutablePreviewLabField<Boolean>(
        label = label,
        initialValue = initialValue,
    ) {
    override fun valueCode(): String = value.toString()
    override fun testValues(): List<Boolean> = listOf(true, false)
    override fun serializer(): KSerializer<Boolean> = Boolean.serializer()

    @Composable
    override fun Content() {
        Checkbox(
            checked = value,
            onCheckedChange = { value = !value },
        )
    }
}
