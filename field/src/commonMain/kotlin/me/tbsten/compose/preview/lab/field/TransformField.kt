package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
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
public open class TransformField<BaseValue, TransformedValue>(
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
    override fun serializer(): KSerializer<TransformedValue>? =
        baseField.serializer()?.let { TransformingSerializer(it, transform, reverse) }

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
public fun <BaseValue, TransformedValue> MutablePreviewLabField<BaseValue>.transform(
    transform: (BaseValue) -> TransformedValue,
    reverse: (TransformedValue) -> BaseValue,
    label: String = this.label,
    initialValue: TransformedValue = transform(this.value),
): TransformField<BaseValue, TransformedValue> = TransformField(
    baseField = this,
    transform = transform,
    reverse = reverse,
    label = label,
    initialValue = initialValue,
)

/**
 * A serializer that transforms values during serialization/deserialization.
 *
 * This serializer wraps a base serializer and applies transform/reverse functions
 * to convert between BaseValue and TransformedValue types.
 *
 * @param baseSerializer The serializer for the base value type.
 * @param transform Function to convert from base value to transformed value (used during deserialization).
 * @param reverse Function to convert from transformed value back to base value (used during serialization).
 */
internal class TransformingSerializer<BaseValue, TransformedValue>(
    private val baseSerializer: KSerializer<BaseValue>,
    private val transform: (BaseValue) -> TransformedValue,
    private val reverse: (TransformedValue) -> BaseValue,
) : KSerializer<TransformedValue> {
    override val descriptor: SerialDescriptor = baseSerializer.descriptor

    override fun serialize(encoder: Encoder, value: TransformedValue) {
        baseSerializer.serialize(encoder, reverse(value))
    }

    override fun deserialize(decoder: Decoder): TransformedValue = transform(baseSerializer.deserialize(decoder))
}
