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
    WithHintField<Value>(this, choices = mapOf(*choices))

/**
 * A field wrapper that adds hint choices below the base field.
 * Users can click on hint chips to quickly apply predefined values.
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
 * // Adding hints to string fields (like common usernames or URLs)
 * @Preview
 * @Composable
 * fun EmailPreview() = PreviewLab {
 *     val email: String = fieldValue {
 *         StringField(label = "Email", initialValue = "user@example.com")
 *             .withHint(
 *                 "Personal" to "john.doe@gmail.com",
 *                 "Work" to "john@company.com",
 *                 "Test" to "test@example.com"
 *             )
 *     }
 *     EmailDisplay(email = email)
 * }
 *
 * // Combining withHint with other field modifiers
 * @Preview
 * @Composable
 * fun PaddingPreview() = PreviewLab {
 *     val padding: Int = fieldValue {
 *         IntField(label = "Padding", initialValue = 16)
 *             .withHint(
 *                 "None" to 0,
 *                 "Small" to 8,
 *                 "Medium" to 16,
 *                 "Large" to 24,
 *                 "XLarge" to 32
 *             )
 *     }
 *     Box(Modifier.padding(padding.dp)) {
 *         Text("Content with padding")
 *     }
 * }
 * ```
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
    override fun testValues(): List<Value> = super.testValues() +
        (baseField.testValues() + choices.values).distinct()

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
