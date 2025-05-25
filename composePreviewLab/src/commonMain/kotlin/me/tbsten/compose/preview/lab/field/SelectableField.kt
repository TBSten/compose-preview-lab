package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable
import me.tbsten.compose.preview.lab.component.SelectButton

open class SelectableField<Value>(
    label: String,
    val choices: List<Value>,
    private val choiceLabel: (Value) -> String = { it.toString() },
    initialValue: Value = choices[0],
) : MutablePreviewLabField<Value>(
    label = label,
    initialValue = initialValue,
) {
    class Builder<Value> internal constructor() {
        internal val choices = mutableListOf<Value>()
        internal var defaultValue: Value? = null
        internal var isDefaultValueSet = false
        fun choice(value: Value, isDefault: Boolean = false) {
            choices.add(value)
            if (isDefault) {
                defaultValue = value
                isDefaultValueSet = true
            }
        }
    }

    @Composable
    override fun Content() {
        SelectButton(
            value = value,
            choices = choices,
            onSelect = { value = it },
            title = choiceLabel,
        )
    }
}

@Suppress("UNCHECKED_CAST")
fun <Value> SelectableField(
    label: String,
    builder: SelectableField.Builder<Value>.() -> Unit,
): SelectableField<Value> {
    val builder = SelectableField.Builder<Value>().apply(builder)
    return SelectableField<Value>(
        label = label,
        choices = builder.choices,
        initialValue = if (builder.isDefaultValueSet) builder.defaultValue as Value else builder.choices[0],
    )
}

@Suppress("FunctionName")
inline fun <reified E : Enum<E>> EnumField(
    label: String,
    initialValue: E,
) = SelectableField<E>(
    label = label,
    choices = enumValues<E>().toList(),
    choiceLabel = { it.name },
    initialValue = initialValue,
)
