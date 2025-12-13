package me.tbsten.compose.preview.lab.field

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ImmutablePreviewLabField
import me.tbsten.compose.preview.lab.MutablePreviewLabField
import me.tbsten.compose.preview.lab.PreviewLabField
import me.tbsten.compose.preview.lab.ui.components.SelectButton

fun <Value> sameType(fields: List<PreviewLabField<out Value>>): (Value) -> PreviewLabField<out Value> = { value: Value ->
    fields.firstOrNull { it.value::class.isInstance(value) }
        ?: error("No field found for value: $value")
}

open class PolymorphicField<Value>(
    label: String,
    initialValue: Value,
    private val fields: List<PreviewLabField<out Value>>,
    private val valueToField: (Value) -> PreviewLabField<out Value> = sameType(fields),
) : MutablePreviewLabField<Value>(
    label = label,
    initialValue = initialValue,
) {
    private var selectedField by mutableStateOf(valueToField(initialValue))

    override var value: Value
        get() = selectedField.value
        set(newValue) {
            val field = valueToField(newValue)
            selectedField = field
            if (field is MutablePreviewLabField<*>) {
                @Suppress("UNCHECKED_CAST")
                (field as MutablePreviewLabField<Value>).value = newValue
            }
        }

    override fun testValues(): List<Value> = fields.flatMap { it.testValues() }

    override fun valueCode(): String = selectedField.valueCode()

    @Composable
    override fun Content() {
        Column {
            SelectButton(
                value = selectedField,
                choices = fields,
                onSelect = { selectedField = it },
                title = { it.label },
            )

            Spacer(Modifier.height(4.dp))

            selectedField.Content()
        }
    }
}

class FixedField<Value>(label: String, value: Value, private val valueCodeProvider: (() -> String)? = null) :
    ImmutablePreviewLabField<Value>(
        label = label,
        initialValue = value,
    ) {
    override fun valueCode() = valueCodeProvider?.invoke() ?: super.valueCode()

    @Composable
    override fun Content() {
        // No editable UI for fixed values
    }
}
