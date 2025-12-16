package me.tbsten.compose.preview.lab.field.serializer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

/**
 * Serializer for Compose [Color] values.
 *
 * Serializes colors as their ARGB integer representation (ULong internally).
 * This allows any color value to be serialized and deserialized without loss.
 *
 * # Usage
 *
 * ```kotlin
 * val colorField = ColorField("Background", Color.Blue)
 * val serializer = colorField.serializer() // Returns ColorSerializer
 * ```
 */
object ColorSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Color", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Color) {
        encoder.encodeLong(value.value.toLong())
    }

    override fun deserialize(decoder: Decoder): Color = Color(decoder.decodeLong().toULong())
}

/**
 * Serializer for Compose [Dp] values.
 *
 * Serializes Dp as its underlying Float value.
 *
 * # Usage
 *
 * ```kotlin
 * val dpField = DpField("Padding", 16.dp)
 * val serializer = dpField.serializer() // Returns DpSerializer
 * ```
 */
object DpSerializer : KSerializer<Dp> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Dp", PrimitiveKind.FLOAT)

    override fun serialize(encoder: Encoder, value: Dp) {
        encoder.encodeFloat(value.value)
    }

    override fun deserialize(decoder: Decoder): Dp = decoder.decodeFloat().dp
}

/**
 * Serializer for Compose [TextUnit] (Sp) values.
 *
 * Serializes TextUnit as its underlying Float value (assumes Sp unit).
 *
 * # Usage
 *
 * ```kotlin
 * val spField = SpField("Font Size", 16.sp)
 * val serializer = spField.serializer() // Returns TextUnitSerializer
 * ```
 */
object TextUnitSerializer : KSerializer<TextUnit> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("TextUnit", PrimitiveKind.FLOAT)

    override fun serialize(encoder: Encoder, value: TextUnit) {
        encoder.encodeFloat(value.value)
    }

    override fun deserialize(decoder: Decoder): TextUnit = decoder.decodeFloat().sp
}

/**
 * Serializer for Compose [Offset] values.
 *
 * Serializes Offset as a structure with x and y Float components.
 *
 * # Usage
 *
 * ```kotlin
 * val offsetField = OffsetField("Position", Offset(50f, 100f))
 * val serializer = offsetField.serializer() // Returns OffsetSerializer
 * ```
 */
object OffsetSerializer : KSerializer<Offset> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Offset") {
        element<Float>("x")
        element<Float>("y")
    }

    override fun serialize(encoder: Encoder, value: Offset) {
        encoder.encodeStructure(descriptor) {
            encodeFloatElement(descriptor, 0, value.x)
            encodeFloatElement(descriptor, 1, value.y)
        }
    }

    override fun deserialize(decoder: Decoder): Offset = decoder.decodeStructure(descriptor) {
        var x = 0f
        var y = 0f
        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                0 -> x = decodeFloatElement(descriptor, 0)
                1 -> y = decodeFloatElement(descriptor, 1)
                CompositeDecoder.DECODE_DONE -> break
                else -> error("Unexpected index: $index")
            }
        }
        Offset(x, y)
    }
}

/**
 * Serializer for Compose [DpOffset] values.
 *
 * Serializes DpOffset as a structure with x and y Float components (in dp).
 *
 * # Usage
 *
 * ```kotlin
 * val dpOffsetField = DpOffsetField("Position", DpOffset(16.dp, 8.dp))
 * val serializer = dpOffsetField.serializer() // Returns DpOffsetSerializer
 * ```
 */
object DpOffsetSerializer : KSerializer<DpOffset> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("DpOffset") {
        element<Float>("x")
        element<Float>("y")
    }

    override fun serialize(encoder: Encoder, value: DpOffset) {
        encoder.encodeStructure(descriptor) {
            encodeFloatElement(descriptor, 0, value.x.value)
            encodeFloatElement(descriptor, 1, value.y.value)
        }
    }

    override fun deserialize(decoder: Decoder): DpOffset = decoder.decodeStructure(descriptor) {
        var x = 0f
        var y = 0f
        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                0 -> x = decodeFloatElement(descriptor, 0)
                1 -> y = decodeFloatElement(descriptor, 1)
                CompositeDecoder.DECODE_DONE -> break
                else -> error("Unexpected index: $index")
            }
        }
        DpOffset(x.dp, y.dp)
    }
}

/**
 * Serializer for Compose [Size] values.
 *
 * Serializes Size as a structure with width and height Float components.
 *
 * # Usage
 *
 * ```kotlin
 * val sizeField = SizeField("Canvas", Size(200f, 150f))
 * val serializer = sizeField.serializer() // Returns SizeSerializer
 * ```
 */
object SizeSerializer : KSerializer<Size> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Size") {
        element<Float>("width")
        element<Float>("height")
    }

    override fun serialize(encoder: Encoder, value: Size) {
        encoder.encodeStructure(descriptor) {
            encodeFloatElement(descriptor, 0, value.width)
            encodeFloatElement(descriptor, 1, value.height)
        }
    }

    override fun deserialize(decoder: Decoder): Size = decoder.decodeStructure(descriptor) {
        var width = 0f
        var height = 0f
        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                0 -> width = decodeFloatElement(descriptor, 0)
                1 -> height = decodeFloatElement(descriptor, 1)
                CompositeDecoder.DECODE_DONE -> break
                else -> error("Unexpected index: $index")
            }
        }
        Size(width, height)
    }
}

/**
 * Serializer for Compose [DpSize] values.
 *
 * Serializes DpSize as a structure with width and height Float components (in dp).
 *
 * # Usage
 *
 * ```kotlin
 * val dpSizeField = DpSizeField("Button Size", DpSize(120.dp, 48.dp))
 * val serializer = dpSizeField.serializer() // Returns DpSizeSerializer
 * ```
 */
object DpSizeSerializer : KSerializer<DpSize> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("DpSize") {
        element<Float>("width")
        element<Float>("height")
    }

    override fun serialize(encoder: Encoder, value: DpSize) {
        encoder.encodeStructure(descriptor) {
            encodeFloatElement(descriptor, 0, value.width.value)
            encodeFloatElement(descriptor, 1, value.height.value)
        }
    }

    override fun deserialize(decoder: Decoder): DpSize = decoder.decodeStructure(descriptor) {
        var width = 0f
        var height = 0f
        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                0 -> width = decodeFloatElement(descriptor, 0)
                1 -> height = decodeFloatElement(descriptor, 1)
                CompositeDecoder.DECODE_DONE -> break
                else -> error("Unexpected index: $index")
            }
        }
        DpSize(width.dp, height.dp)
    }
}
