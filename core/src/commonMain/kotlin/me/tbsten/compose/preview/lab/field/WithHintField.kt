package me.tbsten.compose.preview.lab.field

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Chip
import me.tbsten.compose.preview.lab.ui.components.Text

/**
 * Adds hint choices to a MutablePreviewLabField, allowing users to quickly select from predefined values.
 *
 * @param choices Vararg of pairs where first is the hint label and second is the value
 * @return A WithHintField wrapper that displays the base field with hint choices
 */
fun <Value> MutablePreviewLabField<Value>.withHint(vararg choices: Pair<String, Value>): MutablePreviewLabField<Value> =
    WithHintField<Value>(this, choices = mapOf(*choices))

/**
 * Adds a "Reset" hint that returns the field to its initial value.
 *
 * @return A WithHintField wrapper that includes a reset hint
 */
fun <Value> MutablePreviewLabField<Value>.withInitialValueHint(): MutablePreviewLabField<Value> =
    withHint("Reset" to this.initialValue)

/**
 * A field wrapper that adds hint choices below the base field.
 * Users can click on hint chips to quickly apply predefined values.
 *
 * @param Value The type of value managed by the field
 * @param baseField The underlying field to wrap with hints
 * @param choices Map of hint labels to their corresponding values
 */
class WithHintField<Value> internal constructor(
    private val baseField: MutablePreviewLabField<Value>,
    private val choices: Map<String, Value>,
) : TransformField<Value, Value>(
    baseField = baseField,
    transform = { it },
    reverse = { it },
) {
    /**
     * Finds the deepest non-WithHintField base field by traversing nested WithHintField instances.
     */
    private fun findDeepestBaseField(): MutablePreviewLabField<Value> {
        var current = baseField
        while (current is WithHintField<Value>) {
            current = current.baseField
        }
        return current
    }

    /**
     * Collects all choices from nested WithHintField instances in the chain.
     */
    private fun collectAllChoices(): Map<String, Value> {
        val allChoices = mutableMapOf<String, Value>()

        // Add choices from the current field
        allChoices.putAll(choices)

        // Traverse nested WithHintField instances and collect their choices
        var current = baseField
        while (current is WithHintField<Value>) {
            allChoices.putAll(current.choices)
            current = current.baseField
        }

        return allChoices
    }

    @Composable
    override fun Content() {
        val deepestBaseField = findDeepestBaseField()
        val allChoices = collectAllChoices()

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // Display the Content of the deepest non-WithHintField base field
            deepestBaseField.Content()

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            ) {
                allChoices.forEach { choice ->
                    Chip(
                        selected = value == choice.value,
                        label = { Text(choice.key, style = PreviewLabTheme.typography.label2) },
                        onClick = { onSelected(choice.value) },
                    )
                }
            }
        }
    }

    private fun onSelected(newValue: Value) {
        baseField.value = newValue
    }
}
