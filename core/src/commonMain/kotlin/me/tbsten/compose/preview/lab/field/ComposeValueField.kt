package me.tbsten.compose.preview.lab.field

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.tbsten.compose.preview.lab.component.colorpicker.CommonColorPicker
import me.tbsten.compose.preview.lab.field.NumberField.InputType
import me.tbsten.compose.preview.lab.ui.components.Text

/**
 * Field for editing Compose density-independent pixel (Dp) values
 * 
 * Provides a numeric input field with "dp" suffix for editing Dp measurements.
 * Essential for layout dimensions, spacing, and sizing in Compose UI.
 * Built on top of FloatField with automatic conversion between Float and Dp.
 * 
 * ```kotlin
 * // Basic dp field for padding
 * val padding = fieldValue { DpField("Padding", 16.dp) }
 * 
 * // Size dimension field
 * val buttonSize = fieldValue { DpField("Button Size", 120.dp) }
 * 
 * // Use in component
 * Box(
 *     modifier = Modifier
 *         .size(buttonSize)
 *         .padding(padding)
 * ) {
 *     Text("Content")
 * }
 * ```
 * 
 * @param label Display label for the field
 * @param initialValue Starting Dp value
 * @see FloatField
 * @see SpField
 */
class DpField(label: String, initialValue: Dp) :
    TransformField<Float, Dp>(
        baseField = FloatField(
            label = label,
            initialValue = initialValue.value,
            inputType = InputType.TextField(
                suffix = { Text("dp") },
            ),
        ),
        transform = { it.dp },
        reverse = { it.value },
    )

/**
 * Field for editing Compose scalable pixel (Sp) values for typography
 * 
 * Provides a numeric input field with "sp" suffix for editing text size measurements.
 * Specifically designed for font sizes and text-related dimensions that should
 * scale with user accessibility settings. Built on FloatField with automatic
 * conversion between Float and TextUnit.
 * 
 * ```kotlin
 * // Font size field
 * val fontSize = fieldValue { SpField("Font Size", 16.sp) }
 * 
 * // Title text size
 * val titleSize = fieldValue { SpField("Title Size", 24.sp) }
 * 
 * // Use in text components
 * Text(
 *     text = "Sample Text",
 *     fontSize = fontSize
 * )
 * ```
 * 
 * @param label Display label for the field
 * @param initialValue Starting TextUnit value
 * @see FloatField
 * @see DpField
 */
class SpField(label: String, initialValue: TextUnit) :
    TransformField<Float, TextUnit>(
        baseField = FloatField(
            label = label,
            initialValue = initialValue.value,
            inputType = InputType.TextField(
                suffix = { Text("sp") },
            ),
        ),
        transform = { it.sp },
        reverse = { it.value },
    )

/**
 * Field for editing Compose Offset values (x, y coordinates)
 * 
 * Provides separate input fields for x and y coordinates in pixels.
 * Used for positioning elements or defining translation offsets.
 * Each coordinate can be edited independently through dedicated text fields.
 * 
 * ```kotlin
 * // Position offset field
 * val offset = fieldValue { OffsetField("Position", Offset(50f, 100f)) }
 * 
 * // Translation offset
 * val translation = fieldValue { OffsetField("Translation", Offset.Zero) }
 * 
 * // Use with graphics layer
 * Box(
 *     modifier = Modifier.graphicsLayer {
 *         translationX = offset.x
 *         translationY = offset.y
 *     }
 * )
 * ```
 * 
 * @param label Display label for the field
 * @param initialValue Starting Offset value
 * @see DpOffsetField
 * @see MutablePreviewLabField
 */
class OffsetField(label: String, initialValue: Offset) :
    MutablePreviewLabField<Offset>(
        label = label,
        initialValue = initialValue,
    ) {
    @Composable
    override fun Content() {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TextFieldContent(
                toString = { it.x.toString() },
                toValue = { runCatching { Offset(x = it.toFloat(), y = value.y) } },
                placeholder = { Text("x") },
                modifier = Modifier.weight(1f),
            )

            TextFieldContent(
                toString = { it.y.toString() },
                toValue = { runCatching { Offset(x = value.x, y = it.toFloat()) } },
                placeholder = { Text("y") },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Field for editing Compose DpOffset values (x, y coordinates in Dp)
 * 
 * Provides separate input fields for x and y coordinates in density-independent pixels.
 * Used for layout positioning and spacing that should scale with screen density.
 * Each coordinate can be edited independently with automatic Dp conversion.
 * 
 * ```kotlin
 * // Layout offset field
 * val layoutOffset = fieldValue { DpOffsetField("Offset", DpOffset(16.dp, 8.dp)) }
 * 
 * // Positioning offset
 * val position = fieldValue { DpOffsetField("Position", DpOffset.Zero) }
 * 
 * // Use with offset modifier
 * Text(
 *     text = "Positioned Text",
 *     modifier = Modifier.offset(layoutOffset.x, layoutOffset.y)
 * )
 * ```
 * 
 * @param label Display label for the field
 * @param initialValue Starting DpOffset value
 * @see OffsetField
 * @see MutablePreviewLabField
 */
class DpOffsetField(label: String, initialValue: DpOffset) :
    MutablePreviewLabField<DpOffset>(
        label = label,
        initialValue = initialValue,
    ) {
    @Composable
    override fun Content() {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TextFieldContent(
                toString = { it.x.value.toString() },
                toValue = { runCatching { DpOffset(x = it.toFloat().dp, y = value.y) } },
                placeholder = { Text("x") },
                modifier = Modifier.weight(1f),
            )

            TextFieldContent(
                toString = { it.y.value.toString() },
                placeholder = { Text("y") },
                toValue = { runCatching { DpOffset(x = value.x, y = it.toFloat().dp) } },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Field for editing Compose Size values (width, height in pixels)
 * 
 * Provides separate input fields for width and height dimensions in pixels.
 * Used for defining component sizes, canvas dimensions, or geometric measurements.
 * Each dimension can be edited independently through dedicated text fields.
 * 
 * ```kotlin
 * // Canvas size field
 * val canvasSize = fieldValue { SizeField("Canvas", Size(200f, 150f)) }
 * 
 * // Image dimensions
 * val imageSize = fieldValue { SizeField("Image Size", Size(300f, 200f)) }
 * 
 * // Use with Canvas
 * Canvas(
 *     modifier = Modifier.size(
 *         width = canvasSize.width.dp,
 *         height = canvasSize.height.dp
 *     )
 * ) {
 *     // Drawing operations
 * }
 * ```
 * 
 * @param label Display label for the field
 * @param initialValue Starting Size value
 * @see DpSizeField
 * @see MutablePreviewLabField
 */
class SizeField(label: String, initialValue: Size) :
    MutablePreviewLabField<Size>(
        label = label,
        initialValue = initialValue,
    ) {
    @Composable
    override fun Content() {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TextFieldContent(
                toString = { it.width.toString() },
                toValue = { runCatching { Size(width = it.toFloat(), height = value.height) } },
                placeholder = { Text("width") },
                modifier = Modifier.weight(1f),
            )

            TextFieldContent(
                toString = { it.height.toString() },
                toValue = { runCatching { Size(width = value.width, height = it.toFloat()) } },
                placeholder = { Text("height") },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Field for editing Compose DpSize values (width, height in Dp)
 * 
 * Provides separate input fields for width and height dimensions in density-independent pixels.
 * Used for layout dimensions, component sizing, and spacing that should scale with screen density.
 * Each dimension can be edited independently with automatic Dp conversion.
 * 
 * ```kotlin
 * // Component size field
 * val buttonSize = fieldValue { DpSizeField("Button Size", DpSize(120.dp, 48.dp)) }
 * 
 * // Container dimensions
 * val containerSize = fieldValue { DpSizeField("Container", DpSize(200.dp, 100.dp)) }
 * 
 * // Use with size modifier
 * Button(
 *     onClick = { },
 *     modifier = Modifier.size(buttonSize)
 * ) {
 *     Text("Sized Button")
 * }
 * ```
 * 
 * @param label Display label for the field
 * @param initialValue Starting DpSize value
 * @see SizeField
 * @see MutablePreviewLabField
 */
class DpSizeField(label: String, initialValue: DpSize) :
    MutablePreviewLabField<DpSize>(
        label = label,
        initialValue = initialValue,
    ) {
    @Composable
    override fun Content() {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TextFieldContent(
                toString = { it.width.value.toString() },
                toValue = {
                    runCatching {
                        DpSize(
                            width = it.toFloat().dp,
                            height = value.height,
                        )
                    }
                },
                placeholder = { Text("width") },
                modifier = Modifier.weight(1f),
            )

            TextFieldContent(
                toString = { it.height.value.toString() },
                toValue = { runCatching { DpSize(width = value.width, height = it.toFloat().dp) } },
                placeholder = { Text("height") },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * A field for selecting colors using an interactive color picker.
 *
 * Provides a visual color picker interface allowing users to select colors
 * using HSV sliders and alpha channel controls.
 *
 * @param label The display label for this field
 * @param initialValue The initial color value
 */
class ColorField(label: String, initialValue: Color) :
    MutablePreviewLabField<Color>(
        label = label,
        initialValue = initialValue,
    ) {
    @Composable
    override fun Content() {
        CommonColorPicker(
            color = value,
            onColorSelected = { value = it },
            modifier = Modifier
                .widthIn(max = 180.dp)
                .fillMaxWidth()
                .aspectRatio(3f / 2f),
        )
    }
}
