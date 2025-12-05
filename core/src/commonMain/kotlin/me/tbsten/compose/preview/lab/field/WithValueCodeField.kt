package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable

fun <Value> MutablePreviewLabField<Value>.withValueCode(valueCode: (Value) -> String) = WithValueCodeField(
    baseField = this,
    valueCode = valueCode,
)

class WithValueCodeField<Value>(
    private val baseField: MutablePreviewLabField<Value>,
    private val valueCode: (Value) -> String,
) : MutablePreviewLabField<Value>(
    label = baseField.label,
    initialValue = baseField.initialValue,
) {
    override var value: Value by baseField::value
    override fun valueCode(): String = valueCode(value)
    override fun testValues(): List<Value> = baseField.testValues()

    @Composable
    override fun View() = baseField.View()

    @Composable
    override fun Content() = baseField.Content()
}
