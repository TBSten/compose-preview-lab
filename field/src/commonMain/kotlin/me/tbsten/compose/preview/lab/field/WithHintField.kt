package me.tbsten.compose.preview.lab.field

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import me.tbsten.compose.preview.lab.MutablePreviewLabField
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Chip
import me.tbsten.compose.preview.lab.ui.components.Text

/**
 * Represents a hint choice that can be either a value or an action.
 *
 * @param Value The type of value managed by the field
 */
sealed interface HintChoice<Value> {
    /**
     * A hint choice that sets a specific value when selected.
     *
     * @param value The value to set when this choice is selected
     */
    data class ValueChoice<Value>(val value: Value) : HintChoice<Value>

    /**
     * A hint choice that executes a suspend action when selected.
     *
     * @param action The suspend action to execute when this choice is selected.
     *               The action receives the field as its receiver, allowing direct manipulation.
     */
    data class ActionChoice<Value>(val action: suspend MutablePreviewLabField<Value>.() -> Unit) : HintChoice<Value>
}

/**
 * Adds hint choices to a MutablePreviewLabField, allowing users to quickly select from predefined values.
 *
 * # Usage
 *
 * ```kt
 * // Adding hints to number fields (like font sizes)
 * @Preview
 * @Composable
 * fun TextPreview() = PreviewLab {
 *     val fontSize: Int = fieldValue {
 *         IntField(label = "Font Size", initialValue = 16)
 *             .withHint(
 *                 "Small" to 12,
 *                 "Medium" to 16,
 *                 "Large" to 20,
 *                 "XLarge" to 24
 *             )
 *     }
 *     Text("Sample Text", fontSize = fontSize.sp)
 * }
 *
 * // Adding hints to string fields (like common usernames or URLs)
 * @Preview
 * @Composable
 * fun ProfilePreview() = PreviewLab {
 *     val username: String = fieldValue {
 *         StringField(label = "Username", initialValue = "")
 *             .withHint(
 *                 "Alice" to "alice_wonder",
 *                 "Bob" to "bob_builder",
 *                 "Charlie" to "charlie_choco"
 *             )
 *     }
 *     ProfileCard(username = username)
 * }
 *
 * // Combining withHint with other field modifiers
 * @Preview
 * @Composable
 * fun ApiUrlPreview() = PreviewLab {
 *     val apiUrl: String = fieldValue {
 *         StringField(label = "API URL", initialValue = "https://api.example.com")
 *             .withHint(
 *                 "Production" to "https://api.example.com",
 *                 "Staging" to "https://staging.api.example.com",
 *                 "Development" to "https://dev.api.example.com",
 *                 "Local" to "http://localhost:3000"
 *             )
 *     }
 *     ApiClient(baseUrl = apiUrl)
 * }
 * ```
 *
 * @param choices Vararg of pairs where first is the hint label and second is the value
 * @return A WithHintField wrapper that displays the base field with hint choices
 */
fun <Value> MutablePreviewLabField<Value>.withHint(vararg choices: Pair<String, Value>): MutablePreviewLabField<Value> =
    WithHintField(
        baseField = this,
        choices = choices.associate { (label, value) -> label to HintChoice.ValueChoice(value) },
    )

/**
 * Adds hint choices to a MutablePreviewLabField from a Map.
 *
 * This is a convenience overload that accepts a Map instead of vararg pairs.
 *
 * # Usage
 *
 * ```kt
 * val sizeHints = mapOf("Small" to 12, "Medium" to 16, "Large" to 20)
 *
 * @Preview
 * @Composable
 * fun TextPreview() = PreviewLab {
 *     val fontSize: Int = fieldValue {
 *         IntField(label = "Font Size", initialValue = 16)
 *             .withHint(sizeHints)
 *     }
 *     Text("Sample Text", fontSize = fontSize.sp)
 * }
 * ```
 *
 * @param choices Map of hint labels to their corresponding values
 * @return A WithHintField wrapper that displays the base field with hint choices
 */
fun <Value> MutablePreviewLabField<Value>.withHint(choices: Map<String, Value>): MutablePreviewLabField<Value> = WithHintField(
    baseField = this,
    choices = choices.mapValues { (_, value) -> HintChoice.ValueChoice(value) },
)

/**
 * Adds hint actions to a MutablePreviewLabField, allowing users to execute custom actions.
 *
 * This function allows executing arbitrary actions on the field when a hint is selected.
 * The action receives the field as its receiver, enabling direct manipulation of the field's
 * value or other properties.
 *
 * # Usage
 *
 * ```kt
 * @Preview
 * @Composable
 * fun ListPreview() = PreviewLab {
 *     val items: List<String> = fieldValue {
 *         ListField(StringField(label = "Item", initialValue = ""))
 *             .withHintAction(
 *                 "Add 3 items" to { repeat(3) { value = value + "Item ${value.size + 1}" } },
 *                 "Clear all" to { value = emptyList() }
 *             )
 *     }
 *     ItemList(items = items)
 * }
 * ```
 *
 * @param choices Vararg of pairs where first is the hint label and second is the action to execute
 * @return A WithHintField wrapper that displays the base field with action hints
 * @see withHint
 */
fun <Value> MutablePreviewLabField<Value>.withHintAction(
    vararg choices: Pair<String, suspend MutablePreviewLabField<Value>.() -> Unit>,
): MutablePreviewLabField<Value> = WithHintField(
    baseField = this,
    choices = choices.associate { (label, action) -> label to HintChoice.ActionChoice(action) },
)

/**
 * Adds a single action hint to a MutablePreviewLabField.
 *
 * This is a convenience overload for adding a single action hint with trailing lambda syntax.
 *
 * # Usage
 *
 * ```kt
 * @Preview
 * @Composable
 * fun TextPreview() = PreviewLab {
 *     val text: String = fieldValue {
 *         StringField(label = "Text", initialValue = "hello")
 *             .withHint("Uppercase") { value = value.uppercase() }
 *             .withHint("Clear") { value = "" }
 *     }
 *     Text(text)
 * }
 * ```
 *
 * @param label The label to display for this hint
 * @param action The action to execute when this hint is selected
 * @return A WithHintField wrapper that displays the base field with the action hint
 */
fun <Value> MutablePreviewLabField<Value>.withHint(
    label: String,
    action: suspend MutablePreviewLabField<Value>.() -> Unit,
): MutablePreviewLabField<Value> = WithHintField(
    baseField = this,
    choices = mapOf(label to HintChoice.ActionChoice(action)),
)

/**
 * A field wrapper that adds hint choices below the base field.
 * Users can click on hint chips to quickly apply predefined values or execute actions.
 *
 * # Usage
 *
 * ```kt
 * // Adding hints to number fields (like font sizes)
 * @Preview
 * @Composable
 * fun FontSizePreview() = PreviewLab {
 *     val lineHeight: Int = fieldValue {
 *         IntField(label = "Line Height", initialValue = 24)
 *             .withHint(
 *                 "Compact" to 18,
 *                 "Normal" to 24,
 *                 "Relaxed" to 32
 *             )
 *     }
 *     Text("Multi-line text example", lineHeight = lineHeight.sp)
 * }
 *
 * // Adding action hints for custom operations
 * @Preview
 * @Composable
 * fun ListPreview() = PreviewLab {
 *     val items: List<String> = fieldValue {
 *         ListField(StringField(label = "Item", initialValue = ""))
 *             .withHintAction(
 *                 "Add 3 items" to { repeat(3) { value = value + "Item" } },
 *                 "Clear" to { value = emptyList() }
 *             )
 *     }
 *     ItemList(items = items)
 * }
 * ```
 *
 * @param Value The type of value managed by the field
 * @param baseField The underlying field to wrap with hints
 * @param choices Map of hint labels to their corresponding [HintChoice] (value or action)
 */
class WithHintField<Value> internal constructor(
    private val baseField: MutablePreviewLabField<Value>,
    private val choices: Map<String, HintChoice<Value>>,
) : TransformField<Value, Value>(
    baseField = baseField,
    transform = { it },
    reverse = { it },
) {
    override fun valueCode(): String = baseField.valueCode()
    override fun serializer(): KSerializer<Value>? = baseField.serializer()

    override fun testValues(): List<Value> = super.testValues() +
        (baseField.testValues() + choices.values.filterIsInstance<HintChoice.ValueChoice<Value>>().map { it.value }).distinct()

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
    private fun collectAllChoices(): Map<String, HintChoice<Value>> {
        val allChoices = mutableMapOf<String, HintChoice<Value>>()

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
        val coroutineScope = rememberCoroutineScope()

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
                allChoices.forEach { (label, choice) ->
                    val isSelected = when (choice) {
                        is HintChoice.ValueChoice -> value == choice.value
                        is HintChoice.ActionChoice -> false
                    }
                    Chip(
                        selected = isSelected,
                        label = { Text(label, style = PreviewLabTheme.typography.label2) },
                        onClick = {
                            coroutineScope.launch {
                                onSelected(choice)
                            }
                        },
                    )
                }
            }
        }
    }

    private suspend fun onSelected(choice: HintChoice<Value>) {
        when (choice) {
            is HintChoice.ValueChoice -> baseField.value = choice.value
            is HintChoice.ActionChoice -> choice.action.invoke(baseField)
        }
    }
}
