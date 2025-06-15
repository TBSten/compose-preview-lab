package me.tbsten.compose.preview.lab.field

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.tbsten.compose.preview.lab.component.SelectButton
import me.tbsten.compose.preview.lab.field.SelectableField.Type
import me.tbsten.compose.preview.lab.field.SelectableField.Type.CHIPS
import me.tbsten.compose.preview.lab.field.SelectableField.Type.DROPDOWN

open class SelectableField<Value>(
    label: String,
    val choices: List<Value>,
    private val choiceLabel: (Value) -> String = { it.toString() },
    private val type: Type = DROPDOWN,
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

    enum class Type {
        DROPDOWN,
        CHIPS,
    }

    @Composable
    override fun Content() {
        when (type) {
            DROPDOWN -> SelectButton(
                value = value,
                choices = choices,
                onSelect = { value = it },
                title = choiceLabel,
            )
            CHIPS -> ChipsContent()
        }
    }

    @Composable
    private fun ChipsContent() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            choices.forEach { choice ->
                InputChip(
                    selected = value == choice,
                    label = { Text(choiceLabel(choice)) },
                    onClick = { value = choice },
                )
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun <Value> SelectableField(
    label: String,
    type: Type = DROPDOWN,
    builder: SelectableField.Builder<Value>.() -> Unit,
): SelectableField<Value> {
    val builder = SelectableField.Builder<Value>().apply(builder)
    return SelectableField<Value>(
        label = label,
        choices = builder.choices,
        type = type,
        initialValue = if (builder.isDefaultValueSet) builder.defaultValue as Value else builder.choices[0],
    )
}

@Suppress("FunctionName")
inline fun <reified E : Enum<E>> EnumField(label: String, initialValue: E, type: Type = DROPDOWN,) = SelectableField<E>(
    label = label,
    choices = enumValues<E>().toList(),
    choiceLabel = { it.name },
    type = type,
    initialValue = initialValue,
)
