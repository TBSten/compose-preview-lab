package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import me.tbsten.compose.preview.lab.MutablePreviewLabField
import me.tbsten.compose.preview.lab.field.component.TextFieldContent

/**
 * A field that allows users to input a string value.
 *
 * # Usage
 *
 * ```kt
 * // Basic usage
 * @Preview
 * @Composable
 * fun TextPreview() = PreviewLab {
 *     val text: String = fieldValue { StringField("Text", "Hello World!!") }
 *     Text(text = text)
 * }
 *
 * // With prefix and suffix
 * @Preview
 * @Composable
 * fun UrlPreview() = PreviewLab {
 *     val path: String = fieldValue {
 *         StringField(
 *             label = "Path",
 *             initialValue = "home",
 *             prefix = { Text("https://example.com/") },
 *             suffix = { Icon(Icons.Default.Link, null) }
 *         )
 *     }
 *     Text(text = "URL: https://example.com/$path")
 * }
 *
 * // For multiline text input
 * @Preview
 * @Composable
 * fun DescriptionPreview() = PreviewLab {
 *     val description: String = fieldValue {
 *         StringField(
 *             label = "Description",
 *             initialValue = "Enter your description here..."
 *         )
 *     }
 *     Text(text = description)
 * }
 * ```
 *
 * @param prefix The slot that will be displayed as the prefix of the TextField.
 * @param suffix The slot that will be displayed as the suffix of TextField.
 */
public class StringField(
    label: String,
    initialValue: String,
    private val prefix: (@Composable () -> Unit)? = null,
    private val suffix: (@Composable () -> Unit)? = null,
) : MutablePreviewLabField<String>(
    label = label,
    initialValue = initialValue,
) {
    override fun testValues(): List<String> = super.testValues() +
        listOf(initialValue, "")
            .toSet().toList()

    override fun valueCode(): String {
        val escaped = value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            .replace("$", "\\$")
        return "\"$escaped\""
    }

    override fun serializer(): KSerializer<String> = String.serializer()

    @Composable
    override fun Content() {
        TextFieldContent<String>(
            toValue = { Result.success(it) },
            toString = { it },
            prefix = prefix,
            suffix = suffix,
        )
    }
}

public fun MutablePreviewLabField<String>.withTextHint(): MutablePreviewLabField<String> = withHint(
    "Empty" to "",
    "Short" to "Hello !",
    "Body" to """
            Compose Preview Lab turns @Preview into an interactive Component Playground.
            You can pass parameters to components, enabling more than just static snapshots—making manual testing easier and helping new developers understand components faster.
            Compose Multiplatform is supported.
    """.trimIndent(),
    "Long" to "Very " + "Long ".repeat(100) + "Text",
)
