package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable
import kotlin.jvm.JvmName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import me.tbsten.compose.preview.lab.MutablePreviewLabField
import me.tbsten.compose.preview.lab.field.component.TextFieldContent

internal fun floatValueCode(value: Float): String = when {
    value.isNaN() -> "Float.NaN"
    value == Float.POSITIVE_INFINITY -> "Float.POSITIVE_INFINITY"
    value == Float.NEGATIVE_INFINITY -> "Float.NEGATIVE_INFINITY"
    else -> "${value}f"
}

internal fun doubleValueCode(value: Double): String = when {
    value.isNaN() -> "Double.NaN"
    value == Double.POSITIVE_INFINITY -> "Double.POSITIVE_INFINITY"
    value == Double.NEGATIVE_INFINITY -> "Double.NEGATIVE_INFINITY"
    else -> "$value"
}

/**
 * Abstract class of Field that handles numeric types.
 *
 * # Usage
 *
 * ```kt
 * // Basic text field input
 * @Preview
 * @Composable
 * fun BasicNumberPreview() = PreviewLab {
 *     val count: Int = fieldValue {
 *         IntField("Count", 0)
 *     }
 *     Counter(count = count)
 * }
 * ```
 *
 * @param label label of the field.
 * @param initialValue initial value of the field.
 * @param fromString Function to convert a string to the numeric type of this Field.
 * @param toString Function to convert the numeric type of this Field to a string.
 * @param inputType InputType, specifies how values are made available for input in the UI. See [InputType].
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
    private val valueCode: (Num) -> String,
) : MutablePreviewLabField<Num>(
    label = label,
    initialValue = initialValue,
) {
    override fun valueCode(): String = valueCode.invoke(value)

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

    /**
     * Defines the type of input UI to display for the number field.
     */
    sealed interface InputType {
        /**
         * Text field input with optional prefix and suffix composables.
         *
         * @param prefix Optional composable to display before the input
         * @param suffix Optional composable to display after the input
         */
        data class TextField(val prefix: (@Composable () -> Unit)? = null, val suffix: (@Composable () -> Unit)? = null) :
            InputType

        /**
         * Slider input for selecting values within a range.
         *
         * @param min The minimum value
         * @param max The maximum value
         */
        data class Slider(val min: Int, val max: Int) : InputType
    }
}

/**
 * Field that holds an Int value.
 *
 * # Usage
 *
 * ```kt
 * // Basic usage
 * @Preview
 * @Composable
 * fun CounterPreview() = PreviewLab {
 *     val count: Int = fieldValue {
 *         IntField("Count", 0)
 *     }
 *     Counter(count = count)
 * }
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
        valueCode = { "$it" },
    ) {
    override fun serializer(): KSerializer<Int> = Int.serializer()
}

/**
 * Field that holds an Long value.
 *
 * # Usage
 *
 * ```kt
 * // Basic usage
 * @Preview
 * @Composable
 * fun TimestampPreview() = PreviewLab {
 *     val timestamp: Long = fieldValue {
 *         LongField("Timestamp", 0L)
 *     }
 *     DateDisplay(timestamp = timestamp)
 * }
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
        valueCode = { "${it}L" },
    ) {
    override fun serializer(): KSerializer<Long> = Long.serializer()
}

/**
 * Field that holds an Byte value.
 *
 * # Usage
 *
 * ```kt
 * // Basic usage
 * @Preview
 * @Composable
 * fun BytePreview() = PreviewLab {
 *     val flag: Byte = fieldValue {
 *         ByteField("Flag", 0)
 *     }
 *     FlagDisplay(flag = flag)
 * }
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
        valueCode = { "$it" },
    ) {
    override fun serializer(): KSerializer<Byte> = Byte.serializer()
}

/**
 * Field that holds an Double value.
 *
 * # Usage
 *
 * ```kt
 * // Basic usage
 * @Preview
 * @Composable
 * fun PricePreview() = PreviewLab {
 *     val price: Double = fieldValue {
 *         DoubleField("Price", 99.99)
 *     }
 *     PriceTag(price = price)
 * }
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
        valueCode = { doubleValueCode(it) },
    ) {
    override fun serializer(): KSerializer<Double> = Double.serializer()
}

/**
 * Field that holds an Float value.
 *
 * # Usage
 *
 * ```kt
 * // Basic usage
 * @Preview
 * @Composable
 * fun AlphaPreview() = PreviewLab {
 *     val alpha: Float = fieldValue {
 *         FloatField("Alpha", 0.5f)
 *     }
 *     TransparentBox(alpha = alpha)
 * }
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
        valueCode = { floatValueCode(it) },
    ) {
    override fun serializer(): KSerializer<Float> = Float.serializer()
}

// Int
@JvmName("withIntIncrementHint")
fun MutablePreviewLabField<Int>.withIncrementHint() = withHintAction(
    "+ Increment" to { value += 1 },
)

@JvmName("withIntDecrementHint")
fun MutablePreviewLabField<Int>.withDecrementHint() = withHintAction(
    "- Decrement" to { value -= 1 },
)

@JvmName("withIntIncrementDecrementHint")
fun MutablePreviewLabField<Int>.withIncrementDecrementHint() = withIncrementHint()
    .withDecrementHint()

// Long
@JvmName("withLongIncrementHint")
fun MutablePreviewLabField<Long>.withIncrementHint() = withHintAction(
    "+ Increment" to { value += 1L },
)

@JvmName("withLongDecrementHint")
fun MutablePreviewLabField<Long>.withDecrementHint() = withHintAction(
    "- Decrement" to { value -= 1L },
)

@JvmName("withLongIncrementDecrementHint")
fun MutablePreviewLabField<Long>.withIncrementDecrementHint() = withIncrementHint()
    .withDecrementHint()

// Byte
@JvmName("withByteIncrementHint")
fun MutablePreviewLabField<Byte>.withIncrementHint() = withHintAction(
    "+ Increment" to { value = (value + 1).toByte() },
)

@JvmName("withByteDecrementHint")
fun MutablePreviewLabField<Byte>.withDecrementHint() = withHintAction(
    "- Decrement" to { value = (value - 1).toByte() },
)

@JvmName("withByteIncrementDecrementHint")
fun MutablePreviewLabField<Byte>.withIncrementDecrementHint() = withIncrementHint()
    .withDecrementHint()

// Double
@JvmName("withDoubleIncrementHint")
fun MutablePreviewLabField<Double>.withIncrementHint() = withHintAction(
    "+ Increment" to { value += 1.0 },
)

@JvmName("withDoubleDecrementHint")
fun MutablePreviewLabField<Double>.withDecrementHint() = withHintAction(
    "- Decrement" to { value -= 1.0 },
)

@JvmName("withDoubleIncrementDecrementHint")
fun MutablePreviewLabField<Double>.withIncrementDecrementHint() = withIncrementHint()
    .withDecrementHint()

// Float
@JvmName("withFloatIncrementHint")
fun MutablePreviewLabField<Float>.withIncrementHint() = withHintAction(
    "+ Increment" to { value += 1f },
)

@JvmName("withFloatDecrementHint")
fun MutablePreviewLabField<Float>.withDecrementHint() = withHintAction(
    "- Decrement" to { value -= 1f },
)

@JvmName("withFloatIncrementDecrementHint")
fun MutablePreviewLabField<Float>.withIncrementDecrementHint() = withIncrementHint()
    .withDecrementHint()
