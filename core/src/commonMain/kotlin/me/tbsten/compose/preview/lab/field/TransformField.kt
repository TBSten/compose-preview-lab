package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable
import me.tbsten.compose.preview.lab.MutablePreviewLabField
import me.tbsten.compose.preview.lab.defaultValueCode

/**
 * Field that can have a separate field for holding values and a value for disclosing the status.
 * Used for simple conversion processing when values are wrapped in value classes, etc.
 *
 * # Usage
 *
 * ```kt
 * val stringField: StringField = StringField("number", "42")
 * val intField: TransformField<String, Int> = TransformField(
 *     baseField = stringField,
 *     transform = { it.toIntOrNull() ?: 0 },
 *     reverse = { it.toString() }
 * )
 * ```
 *
 * @param baseField Field that holds the status.
 * @param transform Function to get the converted value.
 * @param reverse Function to return the converted value to its original value.
 */
open class TransformField<BaseValue, TransformedValue>(
    private val baseField: MutablePreviewLabField<BaseValue>,
    private val transform: (BaseValue) -> TransformedValue,
    private val reverse: (TransformedValue) -> BaseValue,
    label: String = baseField.label,
    initialValue: TransformedValue = transform(baseField.value),
    private val valueCode: (TransformedValue) -> String = { defaultValueCode(label) },
) : MutablePreviewLabField<TransformedValue>(
    label = label,
    initialValue = initialValue,
) {
    override fun testValues(): List<TransformedValue> = super.testValues() + baseField.testValues().map(transform)
    override fun valueCode(): String = valueCode(value)

    override var value: TransformedValue
        get() = transform(baseField.value)
        set(value) {
            baseField.value = reverse(value)
        }

    @Composable
    override fun Content() {
        baseField.Content()
    }
}

/**
 * Transforms a [MutablePreviewLabField] to work with a different value type.
 *
 * This extension function creates a [TransformField] that allows you to work with
 * a transformed representation of the original field's value while keeping the
 * original field's UI and behavior.
 *
 * # Usage
 *
 * ```kt
 * val stringField: StringField = StringField("number", "42")
 * val intField: TransformField<String, Int> = stringField.transform(
 *     transform = { it.toIntOrNull() ?: 0 },
 *     reverse = { it.toString() }
 * )
 * ```
 *
 * @param transform Function to convert from base value to transformed value.
 * @param reverse Function to convert from transformed value back to base value.
 * @param label Label for the transformed field. Defaults to the original field's label.
 * @param initialValue Initial value for the transformed field. Defaults to transforming the original field's current value.
 * @return A [TransformField] that presents the transformed value type.
 */
fun <BaseValue, TransformedValue> MutablePreviewLabField<BaseValue>.transform(
    transform: (BaseValue) -> TransformedValue,
    reverse: (TransformedValue) -> BaseValue,
    label: String = this.label,
    initialValue: TransformedValue = transform(this.value),
) = TransformField(
    baseField = this,
    transform = transform,
    reverse = reverse,
    label = label,
    initialValue = initialValue,
)
