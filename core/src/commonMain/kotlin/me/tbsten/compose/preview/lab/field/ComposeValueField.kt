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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.tbsten.compose.preview.lab.MutablePreviewLabField
import me.tbsten.compose.preview.lab.field.NumberField.InputType
import me.tbsten.compose.preview.lab.field.component.TextFieldContent
import me.tbsten.compose.preview.lab.ui.components.Text
import me.tbsten.compose.preview.lab.ui.components.colorpicker.CommonColorPicker

/**
 * Field for editing Compose density-independent pixel (Dp) values
 *
 * Provides a numeric input field with "dp" suffix for editing Dp measurements.
 * Essential for layout dimensions, spacing, and sizing in Compose UI.
 * Built on top of FloatField with automatic conversion between Float and Dp.
 *
 * # Usage
 *
 * ```kotlin
 * // Basic dp field for padding
 * @Preview
 * @Composable
 * fun PaddingPreview() = PreviewLab {
 *     val padding: Dp = fieldValue { DpField("Padding", 16.dp) }
 *
 *     Box(
 *         modifier = Modifier
 *             .background(Color.LightGray)
 *             .padding(padding)
 *     ) {
 *         Text("Padded Content")
 *     }
 * }
 *
 * // Size dimension field
 * @Preview
 * @Composable
 * fun ButtonSizePreview() = PreviewLab {
 *     val buttonSize: Dp = fieldValue { DpField("Button Size", 120.dp) }
 *
 *     Button(
 *         onClick = { },
 *         modifier = Modifier.size(buttonSize)
 *     ) {
 *         Text("Sized Button")
 *     }
 * }
 *
 * // Multiple dp fields for different dimensions
 * @Preview
 * @Composable
 * fun BoxSizePreview() = PreviewLab {
 *     val width: Dp = fieldValue { DpField("Width", 200.dp) }
 *     val height: Dp = fieldValue { DpField("Height", 100.dp) }
 *
 *     Box(
 *         modifier = Modifier
 *             .size(width = width, height = height)
 *             .background(Color.Blue)
 *     )
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
        valueCode = { "${it.value}.dp" },
    )

/**
 * Field for editing Compose scalable pixel (Sp) values for typography
 *
 * Provides a numeric input field with "sp" suffix for editing text size measurements.
 * Specifically designed for font sizes and text-related dimensions that should
 * scale with user accessibility settings. Built on FloatField with automatic
 * conversion between Float and TextUnit.
 *
 * # Usage
 *
 * ```kotlin
 * // Font size field
 * @Preview
 * @Composable
 * fun TextSizePreview() = PreviewLab {
 *     val fontSize: TextUnit = fieldValue { SpField("Font Size", 16.sp) }
 *
 *     Text(
 *         text = "Sample Text",
 *         fontSize = fontSize
 *     )
 * }
 *
 * // Title text size
 * @Preview
 * @Composable
 * fun TitlePreview() = PreviewLab {
 *     val titleSize: TextUnit = fieldValue { SpField("Title Size", 24.sp) }
 *
 *     Text(
 *         text = "Large Title",
 *         fontSize = titleSize,
 *         fontWeight = FontWeight.Bold
 *     )
 * }
 *
 * // Multiple text sizes
 * @Preview
 * @Composable
 * fun TypographyPreview() = PreviewLab {
 *     val headingSize: TextUnit = fieldValue { SpField("Heading", 32.sp) }
 *     val bodySize: TextUnit = fieldValue { SpField("Body", 16.sp) }
 *
 *     Column {
 *         Text("Heading", fontSize = headingSize)
 *         Text("Body text content", fontSize = bodySize)
 *     }
 * }
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
        valueCode = { "${it.value}.sp" },
    )

/**
 * Field for editing Compose Offset values (x, y coordinates)
 *
 * Provides separate input fields for x and y coordinates in pixels.
 * Used for positioning elements or defining translation offsets.
 * Each coordinate can be edited independently through dedicated text fields.
 *
 * # Usage
 *
 * ```kotlin
 * // Position offset field
 * @Preview
 * @Composable
 * fun TranslationPreview() = PreviewLab {
 *     val offset: Offset = fieldValue { OffsetField("Position", Offset(50f, 100f)) }
 *
 *     Box(
 *         modifier = Modifier
 *             .size(100.dp)
 *             .graphicsLayer {
 *                 translationX = offset.x
 *                 translationY = offset.y
 *             }
 *             .background(Color.Blue)
 *     )
 * }
 *
 * // Shadow offset
 * @Preview
 * @Composable
 * fun ShadowOffsetPreview() = PreviewLab {
 *     val shadowOffset: Offset = fieldValue { OffsetField("Shadow Offset", Offset(4f, 4f)) }
 *
 *     Canvas(modifier = Modifier.size(100.dp)) {
 *         drawRect(
 *             color = Color.Gray,
 *             topLeft = shadowOffset
 *         )
 *         drawRect(color = Color.Blue)
 *     }
 * }
 * ```
 *
 * @param label Display label for the field
 * @param initialValue Starting Offset value
 * @see DpOffsetField
 * @see me.tbsten.compose.preview.lab.MutablePreviewLabField
 */
class OffsetField(label: String, initialValue: Offset) :
    MutablePreviewLabField<Offset>(
        label = label,
        initialValue = initialValue,
    ) {
    override fun valueCode(): String = "Offset(x = ${floatValueCode(value.x)}, y = ${floatValueCode(value.y)})"

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
 * # Usage
 *
 * ```kotlin
 * // Layout offset field
 * @Preview
 * @Composable
 * fun OffsetTextPreview() = PreviewLab {
 *     val layoutOffset: DpOffset = fieldValue { DpOffsetField("Offset", DpOffset(16.dp, 8.dp)) }
 *
 *     Text(
 *         text = "Positioned Text",
 *         modifier = Modifier.offset(layoutOffset.x, layoutOffset.y)
 *     )
 * }
 *
 * // Positioning offset with box
 * @Preview
 * @Composable
 * fun PositionedBoxPreview() = PreviewLab {
 *     val position: DpOffset = fieldValue { DpOffsetField("Position", DpOffset.Zero) }
 *
 *     Box(modifier = Modifier.size(200.dp)) {
 *         Box(
 *             modifier = Modifier
 *                 .size(50.dp)
 *                 .offset(position.x, position.y)
 *                 .background(Color.Red)
 *         )
 *     }
 * }
 *
 * // Absolute positioning
 * @Preview
 * @Composable
 * fun AbsoluteOffsetPreview() = PreviewLab {
 *     val offset: DpOffset = fieldValue { DpOffsetField("Absolute Offset", DpOffset(32.dp, 24.dp)) }
 *
 *     Box(modifier = Modifier.size(150.dp).background(Color.LightGray)) {
 *         Icon(
 *             Icons.Default.Star,
 *             contentDescription = null,
 *             modifier = Modifier.absoluteOffset(offset.x, offset.y)
 *         )
 *     }
 * }
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
    override fun valueCode(): String = "DpOffset(x = ${value.x.value}.dp, y = ${value.y.value}.dp)"

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
 * # Usage
 *
 * ```kotlin
 * // Canvas size field
 * @Preview
 * @Composable
 * fun CanvasSizePreview() = PreviewLab {
 *     val canvasSize: Size = fieldValue { SizeField("Canvas", Size(200f, 150f)) }
 *
 *     Canvas(
 *         modifier = Modifier.size(
 *             width = canvasSize.width.dp,
 *             height = canvasSize.height.dp
 *         )
 *     ) {
 *         drawRect(Color.Blue)
 *     }
 * }
 *
 * // Rectangle dimensions
 * @Preview
 * @Composable
 * fun RectangleSizePreview() = PreviewLab {
 *     val rectSize: Size = fieldValue { SizeField("Rectangle", Size(100f, 50f)) }
 *
 *     Canvas(modifier = Modifier.size(200.dp)) {
 *         drawRect(
 *             color = Color.Red,
 *             size = rectSize
 *         )
 *     }
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
    override fun valueCode(): String = "Size(width = ${floatValueCode(value.width)}, height = ${floatValueCode(value.height)})"

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
 * # Usage
 *
 * ```kotlin
 * // Component size field
 * @Preview
 * @Composable
 * fun ButtonSizePreview() = PreviewLab {
 *     val buttonSize: DpSize = fieldValue { DpSizeField("Button Size", DpSize(120.dp, 48.dp)) }
 *
 *     Button(
 *         onClick = { },
 *         modifier = Modifier.size(buttonSize)
 *     ) {
 *         Text("Sized Button")
 *     }
 * }
 *
 * // Container dimensions
 * @Preview
 * @Composable
 * fun ContainerPreview() = PreviewLab {
 *     val containerSize: DpSize = fieldValue { DpSizeField("Container", DpSize(200.dp, 100.dp)) }
 *
 *     Box(
 *         modifier = Modifier
 *             .size(containerSize)
 *             .background(Color.LightGray)
 *     ) {
 *         Text("Container Content")
 *     }
 * }
 *
 * // Image dimensions
 * @Preview
 * @Composable
 * fun ImageSizePreview() = PreviewLab {
 *     val imageSize: DpSize = fieldValue { DpSizeField("Image Size", DpSize(150.dp, 150.dp)) }
 *
 *     Image(
 *         painter = painterResource(R.drawable.sample),
 *         contentDescription = null,
 *         modifier = Modifier.size(imageSize)
 *     )
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
    override fun valueCode(): String = "DpSize(width = ${value.width.value}.dp, height = ${value.height.value}.dp)"

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
 * # Usage
 *
 * ```kotlin
 * // Basic color picker
 * @Preview
 * @Composable
 * fun ColorPickerPreview() = PreviewLab {
 *     val backgroundColor: Color = fieldValue { ColorField("Background", Color.Blue) }
 *
 *     Box(
 *         modifier = Modifier
 *             .size(100.dp)
 *             .background(backgroundColor)
 *     )
 * }
 *
 * // Multiple color fields
 * @Preview
 * @Composable
 * fun MultiColorPreview() = PreviewLab {
 *     val backgroundColor: Color = fieldValue { ColorField("Background", Color.White) }
 *     val textColor: Color = fieldValue { ColorField("Text Color", Color.Black) }
 *
 *     Box(
 *         modifier = Modifier
 *             .size(150.dp)
 *             .background(backgroundColor),
 *         contentAlignment = Alignment.Center
 *     ) {
 *         Text("Styled Text", color = textColor)
 *     }
 * }
 *
 * // Border color picker
 * @Preview
 * @Composable
 * fun BorderColorPreview() = PreviewLab {
 *     val borderColor: Color = fieldValue { ColorField("Border", Color.Red) }
 *
 *     Box(
 *         modifier = Modifier
 *             .size(100.dp)
 *             .border(3.dp, borderColor)
 *             .background(Color.LightGray)
 *     )
 * }
 * ```
 *
 * @param label The display label for this field
 * @param initialValue The initial color value
 */
class ColorField(label: String, initialValue: Color) :
    MutablePreviewLabField<Color>(
        label = label,
        initialValue = initialValue,
    ) {
    override fun testValues(): List<Color> = predefinedColorNames.keys.toList()

    override fun valueCode(): String {
        val color: Color = this.value
        val predefinedName = predefinedColorNames[color]
        if (predefinedName != null) return predefinedName

        val argb = color.toArgb()
        val hex = argb.toUInt().toString(16).uppercase().padStart(8, '0')
        return if (color.alpha == 1f) {
            "Color(0xFF${hex.substring(2)})"
        } else {
            "Color(0x$hex)"
        }
    }

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

    companion object {
        /**
         * A map of predefined Compose [Color] constants to their Kotlin code representations.
         *
         * Used by [ColorField.valueCode] to output readable color names (e.g., `Color.Red`)
         * instead of hex values when the selected color matches a predefined constant.
         *
         * Also used by [withPredefinedColorHint] to provide quick-select hints for common colors.
         *
         * Includes the following colors:
         * - Primary: Red, Green, Blue, Black, White
         * - Secondary: Cyan, Magenta, Yellow
         * - Grays: Gray, DarkGray, LightGray
         * - Special: Transparent, Unspecified
         */
        val predefinedColorNames = mapOf(
            Color.Red to "Color.Red",
            Color.Green to "Color.Green",
            Color.Blue to "Color.Blue",
            Color.Black to "Color.Black",
            Color.White to "Color.White",
            Color.Cyan to "Color.Cyan",
            Color.Magenta to "Color.Magenta",
            Color.Yellow to "Color.Yellow",
            Color.Gray to "Color.Gray",
            Color.DarkGray to "Color.DarkGray",
            Color.LightGray to "Color.LightGray",
            Color.Transparent to "Color.Transparent",
            Color.Unspecified to "Color.Unspecified",
        )
    }
}

/**
 * Adds predefined color hints to a Color field for quick selection.
 *
 * Wraps the field with hint buttons for all colors in [ColorField.predefinedColorNames],
 * allowing users to quickly select common colors like `Color.Red`, `Color.Blue`, etc.
 *
 * # Usage
 *
 * ```kotlin
 * @Preview
 * @Composable
 * fun ColorPreview() = PreviewLab {
 *     val backgroundColor = fieldValue {
 *         ColorField("Background", Color.White).withPredefinedColorHint()
 *     }
 *
 *     Box(
 *         modifier = Modifier
 *             .size(100.dp)
 *             .background(backgroundColor)
 *     )
 * }
 * ```
 *
 * The hints appear as clickable buttons below the color picker, labeled with the color names
 * (e.g., "Color.Red", "Color.Blue"). Clicking a hint sets the field to that color value.
 *
 * @return A new field wrapped with predefined color hints
 * @see ColorField
 * @see ColorField.predefinedColorNames
 * @see withHint
 */
fun MutablePreviewLabField<Color>.withPredefinedColorHint() = withHint(
    *ColorField.predefinedColorNames
        .map { (color, name) -> name to color }
        .toTypedArray(),
)
