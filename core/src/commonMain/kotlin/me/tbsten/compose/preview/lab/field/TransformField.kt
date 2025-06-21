package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable

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
