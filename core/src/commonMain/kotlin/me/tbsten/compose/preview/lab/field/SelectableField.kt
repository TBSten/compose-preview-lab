package me.tbsten.compose.preview.lab.field

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.component.SelectButton
import me.tbsten.compose.preview.lab.field.SelectableField.Type
import me.tbsten.compose.preview.lab.field.SelectableField.Type.CHIPS
import me.tbsten.compose.preview.lab.field.SelectableField.Type.DROPDOWN
import me.tbsten.compose.preview.lab.ui.components.Chip
import me.tbsten.compose.preview.lab.ui.components.Text

/**
 * A field that allows selection of one option from a list of specified choices.
 *
 * @param choiceLabel Text to be displayed in the UI to select a choice.
 * @param type Select UI type, default is [Type.DROPDOWN]. See also [SelectableField.Type].
 */
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
        internal val choices = mutableListOf<Pair<String, Value>>()
        internal var defaultValue: Value? = null
        internal var isDefaultValueSet = false
        fun choice(value: Value, label: String = value.toString(), isDefault: Boolean = false) {
            choices.add(label to value)
            if (isDefault) {
                defaultValue = value
                isDefaultValueSet = true
            }
        }
    }

    /**
     * The type of UI to select.
     */
    enum class Type {
        /**
         * Dropdown menu.
         */
        DROPDOWN,

        /**
         * Chips that can be selected.
         */
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
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        ) {
            choices.forEach { choice ->
                Chip(
                    selected = value == choice,
                    label = { Text(choiceLabel(choice)) },
                    onClick = { value = choice },
                )
            }
        }
    }
}

/**
 * Create a [SelectableField] with the given label and choices.
 *
 * @see SelectableField
 */
@Suppress("UNCHECKED_CAST")
fun <Value> SelectableField(
    label: String,
    type: Type = DROPDOWN,
    builder: SelectableField.Builder<Value>.() -> Unit,
): SelectableField<Value> {
    val builder = SelectableField.Builder<Value>().apply(builder)
    return SelectableField<Value>(
        label = label,
        choices = builder.choices.map { it.second },
        type = type,
        choiceLabel = { choice -> builder.choices.first { it.second == choice }.first },
        initialValue = if (builder.isDefaultValueSet) builder.defaultValue as Value else builder.choices[0].second,
    )
}

/**
 * Create a [SelectableField] from enum class values.
 *
 * @see SelectableField
 */
@Suppress("FunctionName")
inline fun <reified E : Enum<E>> EnumField(
    label: String,
    initialValue: E,
    type: Type = DROPDOWN,
    noinline choiceLabel: (E) -> String = { it.name },
) = SelectableField<E>(
    label = label,
    choices = enumValues<E>().toList(),
    choiceLabel = choiceLabel,
    type = type,
    initialValue = initialValue,
)

fun <Value> List<Value>.toField(
    label: String,
    choiceLabel: (Value) -> String = { it.toString() },
    type: Type = DROPDOWN,
    initialValue: Value = this[0],
): SelectableField<Value> = SelectableField(
    label = label,
    choices = this,
    choiceLabel = choiceLabel,
    type = type,
    initialValue = initialValue,
)
