package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable

/**
 * Abstract class of Field that handles numeric types.
 *
 * @param label label of the field.
 * @param initialValue initial value of the field.
 * @param fromString Function to convert a string to the numeric type of this Field.
 * @param toString Function to convert the numeric type of this Field to a string.
 * @param inputType InputType, specifies how values are made available for input in the UI. [See InputType.
 *
 * @see IntField
 * @see LongField
 * @see ByteField
 * @see DoubleField
 * @see FloatField
 * @see InputType
 */
abstract class NumberField<Num : Number>(
    label: String,
    initialValue: Num,
    private val fromString: (String) -> Num,
    private val toString: (Num) -> String = { it.toString() },
    private val inputType: InputType = InputType.TextField(),
) : MutablePreviewLabField<Num>(
    label = label,
    initialValue = initialValue,
) {
    @Composable
    override fun Content() = when (inputType) {
        is InputType.TextField -> TextFieldContent(
            toString = {
                toString(it)
            },
            toValue = {
                runCatching {
                    fromString(it)
                }
            },
            prefix = inputType.prefix,
            suffix = inputType.suffix,
        )
        is InputType.Slider -> TODO("InputType.Slider")
    }

    sealed interface InputType {
        data class TextField(val prefix: (@Composable () -> Unit)? = null, val suffix: (@Composable () -> Unit)? = null) :
            InputType

        data class Slider(val min: Int, val max: Int) : InputType
    }
}

/**
 * Field that holds an Int value.
 *
 * ```kt
 * val hours = fieldValue { IntField("hours", 0, inputType = NumberField.InputType.TextField) }
 * ```
 *
 * @see NumberField
 * @see InputType
 */
open class IntField(label: String, initialValue: Int, inputType: InputType = InputType.TextField()) :
    NumberField<Int>(
        label = label,
        initialValue = initialValue,
        fromString = { it.toInt() },
        toString = { it.toString() },
        inputType = inputType,
    )

/**
 * Field that holds an Long value.
 *
 * ```kt
 * val currentTimeMillis = fieldValue { LongField("currentTimeMillis", 0L, inputType = NumberField.InputType.TextField) }
 * ```
 *
 * @see NumberField
 * @see InputType
 */
open class LongField(label: String, initialValue: Long, inputType: InputType = InputType.TextField()) :
    NumberField<Long>(
        label = label,
        initialValue = initialValue,
        fromString = { it.toLong() },
        toString = { it.toString() },
        inputType = inputType,
    )

/**
 * Field that holds an Byte value.
 *
 * ```kt
 * val bytes = fieldValue { ByteField("bytes", 0, inputType = NumberField.InputType.TextField) }
 * ```
 *
 * @see NumberField
 * @see InputType
 */
open class ByteField(label: String, initialValue: Byte, inputType: InputType = InputType.TextField()) :
    NumberField<Byte>(
        label = label,
        initialValue = initialValue,
        fromString = { it.toByte() },
        toString = { it.toString() },
        inputType = inputType,
    )

/**
 * Field that holds an Double value.
 *
 * ```kt
 * val price = fieldValue { DoubleField("price", 123.45d, inputType = NumberField.InputType.TextField) }
 * ```
 *
 * @see NumberField
 * @see InputType
 */
open class DoubleField(label: String, initialValue: Double, inputType: InputType = InputType.TextField()) :
    NumberField<Double>(
        label = label,
        initialValue = initialValue,
        fromString = { it.toDouble() },
        toString = { it.toString() },
        inputType = inputType,
    )

/**
 * Field that holds an Float value.
 *
 * ```kt
 * val alpha = fieldValue { FloatField("alpha", 0.5f, inputType = NumberField.InputType.TextField) }
 * ```
 *
 * @see NumberField
 * @see InputType
 */
open class FloatField(label: String, initialValue: Float, inputType: InputType = InputType.TextField()) :
    NumberField<Float>(
        label = label,
        initialValue = initialValue,
        fromString = { it.toFloat() },
        toString = { it.toString() },
        inputType = inputType,
    )
