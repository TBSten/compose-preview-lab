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
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
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
import me.tbsten.compose.preview.lab.field.ScreenSize

/**
 * Serializer for Compose [Color] values.
 *
 * Serializes colors as their ARGB integer representation (ULong internally).
 * This allows any color value to be serialized and deserialized without loss.
 */
@OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
internal object ColorSerializer : KSerializer<Color> {
    private val baseSerializer = ColorData.serializer()
    override val descriptor: SerialDescriptor = baseSerializer.descriptor

    override fun serialize(encoder: Encoder, value: Color) = baseSerializer.serialize(
        encoder,
        value.let {
            ColorData(it.red, it.green, it.blue, it.alpha)
        },
    )

    override fun deserialize(decoder: Decoder): Color = baseSerializer.deserialize(
        decoder,
    ).let {
        Color(it.red, it.green, it.blue, it.alpha)
    }
}

@Serializable
private data class ColorData(public val red: Float, public val green: Float, public val blue: Float, public val alpha: Float)

/**
 * Serializer for Compose [Dp] values.
 *
 * Serializes Dp as its underlying Float value.
 */
internal object DpSerializer : KSerializer<Dp> {
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
 */
internal object TextUnitSerializer : KSerializer<TextUnit> {
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
 */
internal object OffsetSerializer : KSerializer<Offset> {
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
 */
internal object DpOffsetSerializer : KSerializer<DpOffset> {
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
 */
internal object SizeSerializer : KSerializer<Size> {
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
 */
internal object DpSizeSerializer : KSerializer<DpSize> {
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

/**
 * Serializer for [ScreenSize] values.
 *
 * Serializes ScreenSize as a structure with width, height (in dp) and label.
 */
internal object ScreenSizeSerializer : KSerializer<ScreenSize> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ScreenSize") {
        element<Float>("width")
        element<Float>("height")
        element<String>("label")
    }

    override fun serialize(encoder: Encoder, value: ScreenSize) {
        encoder.encodeStructure(descriptor) {
            encodeFloatElement(descriptor, 0, value.width.value)
            encodeFloatElement(descriptor, 1, value.height.value)
            encodeStringElement(descriptor, 2, value.label)
        }
    }

    override fun deserialize(decoder: Decoder): ScreenSize = decoder.decodeStructure(descriptor) {
        var width = 0f
        var height = 0f
        var label = ""
        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                0 -> width = decodeFloatElement(descriptor, 0)
                1 -> height = decodeFloatElement(descriptor, 1)
                2 -> label = decodeStringElement(descriptor, 2)
                CompositeDecoder.DECODE_DONE -> break
                else -> error("Unexpected index: $index")
            }
        }
        ScreenSize(width.dp, height.dp, label)
    }
}
