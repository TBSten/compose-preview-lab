package me.tbsten.compose.preview.lab.field

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.component.Divider
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
    @Composable
    override fun Content() {
        Column {
            Box(
                modifier = Modifier
                    .padding(start = 16.dp, end = 6.dp),
            ) { super.Content() }

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            ) {
                choices.forEach { choice ->
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
