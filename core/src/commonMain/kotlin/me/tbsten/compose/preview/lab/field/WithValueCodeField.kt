package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable
import me.tbsten.compose.preview.lab.MutablePreviewLabField

/**
 * Wraps this field with a custom [me.tbsten.compose.preview.lab.PreviewLabField.valueCode] implementation.
 *
 * Use this extension function to customize how the field's current value is converted to
 * Kotlin code in the Inspector's Code tab, without creating a new field class.
 *
 * # Usage
 *
 * ```kotlin
 * @Preview
 * @Composable
 * fun FontWeightPreview() = PreviewLab {
 *     val fontWeight = fieldValue {
 *         SelectableField(
 *             label = "Font Weight",
 *             choices = listOf(FontWeight.Normal, FontWeight.Bold, FontWeight.W500),
 *             choiceLabel = { it.toString() },
 *         ).withValueCode { weight ->
 *             when (weight) {
 *                 FontWeight.Normal -> "FontWeight.Normal"
 *                 FontWeight.Bold -> "FontWeight.Bold"
 *                 else -> "FontWeight(${weight.weight})"
 *             }
 *         }
 *     }
 *
 *     Text("Sample", fontWeight = fontWeight)
 * }
 * ```
 *
 * # When to use
 *
 * - When using [SelectableField] with enum or sealed class values
 * - When the default `toString()` representation isn't valid Kotlin code
 * - When you want to use named constants (e.g., `Color.Red`) instead of constructor calls
 *
 * @param valueCode A function that converts the current value to a Kotlin code string
 * @return A new field with the custom valueCode implementation
 * @see me.tbsten.compose.preview.lab.PreviewLabField.valueCode
 * @see WithValueCodeField
 */
fun <Value> MutablePreviewLabField<Value>.withValueCode(valueCode: (Value) -> String) = WithValueCodeField(
    baseField = this,
    valueCode = valueCode,
)

/**
 * A wrapper field that customizes the [valueCode] output of another field.
 *
 * This class delegates all functionality to the base field except for [valueCode],
 * which uses the provided lambda to generate Kotlin code strings.
 *
 * In most cases, use the [withValueCode] extension function instead of instantiating this class directly.
 *
 * @param Value The type of value this field holds
 * @param baseField The underlying field to wrap
 * @param valueCode A function that converts the current value to a Kotlin code string
 * @see withValueCode
 * @see me.tbsten.compose.preview.lab.PreviewLabField.valueCode
 */
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
