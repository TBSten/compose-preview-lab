package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable
import kotlinx.serialization.KSerializer
import me.tbsten.compose.preview.lab.MutablePreviewLabField

/**
 * Wraps this field with a custom [me.tbsten.compose.preview.lab.PreviewLabField.serializer] implementation.
 *
 * Use this extension function to provide a custom serializer for fields that don't have
 * built-in serialization support, or to override the default serializer with a custom one.
 *
 * # Usage
 *
 * ```kotlin
 * @Serializable
 * enum class Theme { Light, Dark, System }
 *
 * @Preview
 * @Composable
 * fun ThemePreview() = PreviewLab {
 *     val theme = fieldValue {
 *         SelectableField(
 *             label = "Theme",
 *             choices = Theme.entries,
 *             choiceLabel = { it.name },
 *         ).withSerializer(Theme.serializer())
 *     }
 *
 *     AppTheme(theme = theme)
 * }
 * ```
 *
 * # When to use
 *
 * - When using [SelectableField] with serializable enum or sealed class values
 * - When the field type has a custom serializer that differs from the default
 * - When you need to enable serialization for fields that return null by default
 *
 * @param serializer The KSerializer to use for this field's value type
 * @return A new field with the custom serializer implementation
 * @see me.tbsten.compose.preview.lab.PreviewLabField.serializer
 * @see WithSerializerField
 */
fun <Value> MutablePreviewLabField<Value>.withSerializer(serializer: KSerializer<Value>): MutablePreviewLabField<Value> =
    WithSerializerField(
        baseField = this,
        serializer = serializer,
    )

/**
 * A wrapper field that customizes the [serializer] output of another field.
 *
 * This class delegates all functionality to the base field except for [serializer],
 * which returns the provided serializer instance.
 *
 * In most cases, use the [withSerializer] extension function instead of instantiating this class directly.
 *
 * @param Value The type of value this field holds
 * @param baseField The underlying field to wrap
 * @param serializer The KSerializer to use for this field's value type
 * @see withSerializer
 * @see me.tbsten.compose.preview.lab.PreviewLabField.serializer
 */
class WithSerializerField<Value>(
    private val baseField: MutablePreviewLabField<Value>,
    private val serializer: KSerializer<Value>,
) : MutablePreviewLabField<Value>(
    label = baseField.label,
    initialValue = baseField.initialValue,
) {
    override var value: Value by baseField::value
    override fun valueCode(): String = baseField.valueCode()
    override fun testValues(): List<Value> = baseField.testValues()
    override fun serializer(): KSerializer<Value> = serializer

    @Composable
    override fun View() = baseField.View()

    @Composable
    override fun Content() = baseField.Content()
}
