package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable

/**
 * Field that can have a separate field for holding values and a value for disclosing the status.
 * Used for simple conversion processing when values are wrapped in value classes, etc.
 *
 * @param baseField Field that holds the status.
 * @param transform Function to get the converted value.
 * @param reverse Function to return the converted value to its original value.
 */
open class TransformField<BaseValue, TransformedValue>(
    private val baseField: MutablePreviewLabField<BaseValue>,
    private val transform: (BaseValue) -> TransformedValue,
    private val reverse: (TransformedValue) -> BaseValue,
    label: String = baseField.label,
    initialValue: TransformedValue = transform(baseField.value),
) : MutablePreviewLabField<TransformedValue>(
    label = label,
    initialValue = initialValue,
) {
    override var value: TransformedValue
        get() = transform(baseField.value)
        set(value) {
            baseField.value = reverse(value)
        }

    @Composable
    override fun Content() {
        baseField.Content()
    }
}
