package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable
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
 *
 * // With prefix and suffix for units
 * @Preview
 * @Composable
 * fun PricePreview() = PreviewLab {
 *     val price: Double = fieldValue {
 *         DoubleField(
 *             label = "Price",
 *             initialValue = 99.99,
 *             inputType = NumberField.InputType.TextField(
 *                 prefix = { Text("$") },
 *                 suffix = { Text("USD") }
 *             )
 *         )
 *     }
 *     PriceDisplay(price = price)
 * }
 *
 * // Multiple number fields for complex inputs
 * @Preview
 * @Composable
 * fun DimensionsPreview() = PreviewLab {
 *     val width: Int = fieldValue { IntField("Width", 100) }
 *     val height: Int = fieldValue { IntField("Height", 200) }
 *     Rectangle(width = width, height = height)
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
public abstract class NumberField<Num : Number>(
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
    override fun Content(): Unit = when (inputType) {
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
    public sealed interface InputType {
        /**
         * Text field input with optional prefix and suffix composables.
         *
         * @param prefix Optional composable to display before the input
         * @param suffix Optional composable to display after the input
         */
        public data class TextField(
            public val prefix: (@Composable () -> Unit)? = null,
            public val suffix: (@Composable () -> Unit)? = null
        ) : InputType

        /**
         * Slider input for selecting values within a range.
         *
         * @param min The minimum value
         * @param max The maximum value
         */
        public data class Slider(public val min: Int, public val max: Int) : InputType
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
 *
 * // With range constraints (hint values)
 * @Preview
 * @Composable
 * fun AgePreview() = PreviewLab {
 *     val age: Int = fieldValue {
 *         IntField("Age", 25)
 *     }
 *     UserProfile(age = age)
 * }
 *
 * // With prefix/suffix for units
 * @Preview
 * @Composable
 * fun DurationPreview() = PreviewLab {
 *     val duration: Int = fieldValue {
 *         IntField(
 *             label = "Duration",
 *             initialValue = 30,
 *             inputType = NumberField.InputType.TextField(
 *                 suffix = { Text("minutes") }
 *             )
 *         )
 *     }
 *     Timer(durationMinutes = duration)
 * }
 * ```
 *
 * @see NumberField
 * @see InputType
 */
public open class IntField(label: String, initialValue: Int, inputType: InputType = InputType.TextField()) :
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
 *
 * // With range constraints (hint values)
 * @Preview
 * @Composable
 * fun FilePreview() = PreviewLab {
 *     val fileSize: Long = fieldValue {
 *         LongField("File Size (bytes)", 1024L)
 *         // Common file sizes: 0L, 1024L, 1048576L (1MB), 1073741824L (1GB)
 *     }
 *     FileSizeDisplay(sizeInBytes = fileSize)
 * }
 *
 * // With prefix/suffix for units
 * @Preview
 * @Composable
 * fun MemoryPreview() = PreviewLab {
 *     val memory: Long = fieldValue {
 *         LongField(
 *             label = "Memory",
 *             initialValue = 512L,
 *             inputType = NumberField.InputType.TextField(
 *                 suffix = { Text("MB") }
 *             )
 *         )
 *     }
 *     MemoryUsageIndicator(memoryMB = memory)
 * }
 * ```
 *
 * @see NumberField
 * @see InputType
 */
public open class LongField(label: String, initialValue: Long, inputType: InputType = InputType.TextField()) :
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
 *
 * // With range constraints (hint values)
 * @Preview
 * @Composable
 * fun ColorComponentPreview() = PreviewLab {
 *     val red: Byte = fieldValue {
 *         ByteField("Red", 128.toByte())
 *     }
 *     ColorPreview(redComponent = red)
 * }
 *
 * // With prefix/suffix for context
 * @Preview
 * @Composable
 * fun NetworkPreview() = PreviewLab {
 *     val segment: Byte = fieldValue {
 *         ByteField(
 *             label = "IP Segment",
 *             initialValue = 1,
 *             inputType = NumberField.InputType.TextField(
 *                 prefix = { Text("192.168.0.") }
 *             )
 *         )
 *     }
 *     NetworkConfig(lastSegment = segment)
 * }
 * ```
 *
 * @see NumberField
 * @see InputType
 */
public open class ByteField(label: String, initialValue: Byte, inputType: InputType = InputType.TextField()) :
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
 *
 * // With range constraints (hint values)
 * @Preview
 * @Composable
 * fun WeightPreview() = PreviewLab {
 *     val weight: Double = fieldValue {
 *         DoubleField("Weight", 70.5)
 *         // Common weights: 0.0, 50.0, 70.5, 100.0, 150.0
 *     }
 *     WeightDisplay(kilograms = weight)
 * }
 *
 * // With prefix/suffix for currency
 * @Preview
 * @Composable
 * fun CurrencyPreview() = PreviewLab {
 *     val amount: Double = fieldValue {
 *         DoubleField(
 *             label = "Amount",
 *             initialValue = 1234.56,
 *             inputType = NumberField.InputType.TextField(
 *                 prefix = { Text("$") },
 *                 suffix = { Text("USD") }
 *             )
 *         )
 *     }
 *     PaymentSummary(amount = amount)
 * }
 * ```
 *
 * @see NumberField
 * @see InputType
 */
public open class DoubleField(label: String, initialValue: Double, inputType: InputType = InputType.TextField()) :
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
 *
 * // With range constraints (hint values)
 * @Preview
 * @Composable
 * fun ProgressPreview() = PreviewLab {
 *     val progress: Float = fieldValue {
 *         FloatField("Progress", 0.5f)
 *         // Progress values: 0.0f, 0.25f, 0.5f, 0.75f, 1.0f
 *     }
 *     ProgressBar(progress = progress)
 * }
 *
 * // With prefix/suffix for percentage
 * @Preview
 * @Composable
 * fun OpacityPreview() = PreviewLab {
 *     val opacity: Float = fieldValue {
 *         FloatField(
 *             label = "Opacity",
 *             initialValue = 0.8f,
 *             inputType = NumberField.InputType.TextField(
 *                 suffix = { Text("%") }
 *             )
 *         )
 *     }
 *     ImageWithOpacity(opacity = opacity)
 * }
 * ```
 *
 * @see NumberField
 * @see InputType
 */
public open class FloatField(label: String, initialValue: Float, inputType: InputType = InputType.TextField()) :
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
