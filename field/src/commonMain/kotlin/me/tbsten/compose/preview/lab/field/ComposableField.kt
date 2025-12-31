package me.tbsten.compose.preview.lab.field

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ImmutablePreviewLabField
import me.tbsten.compose.preview.lab.field.SelectableField.Type
import me.tbsten.compose.preview.lab.field.SelectableField.Type.DROPDOWN
import me.tbsten.compose.preview.lab.ui.LocalTextStyle
import me.tbsten.compose.preview.lab.ui.components.SelectButton
import me.tbsten.compose.preview.lab.ui.util.thenIf
import me.tbsten.compose.preview.lab.ui.util.thenIfNotNull
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

/**
 * A field that allows selecting from predefined Composable content options.
 *
 * # Usage
 *
 * ```kt
 * // Basic usage with default choices
 * @Preview
 * @Composable
 * fun ContentPreview() = PreviewLab {
 *     val content: @Composable () -> Unit = fieldValue {
 *         ComposableField(
 *             label = "Content",
 *             initialValue = ComposableFieldValue.Red32X32
 *         )
 *     }
 *     MyContainer(content = content)
 * }
 *
 * // With custom choices
 * @Preview
 * @Composable
 * fun IconSlotPreview() = PreviewLab {
 *     val icon: @Composable () -> Unit = fieldValue {
 *         ComposableField(
 *             label = "Icon",
 *             initialValue = ComposableFieldValue.Empty,
 *             choices = listOf(
 *                 ComposableFieldValue.Empty,
 *                 ComposableFieldValue("Home Icon") { Icon(Icons.Default.Home, null) },
 *                 ComposableFieldValue("Search Icon") { Icon(Icons.Default.Search, null) }
 *             )
 *         )
 *     }
 *     MyButton(icon = icon)
 * }
 *
 * // With text content options
 * @Preview
 * @Composable
 * fun HeaderPreview() = PreviewLab {
 *     val header: @Composable () -> Unit = fieldValue {
 *         ComposableField(
 *             label = "Header",
 *             initialValue = ComposableFieldValue.HeadingText,
 *             choices = listOf(
 *                 ComposableFieldValue.HeadingText,
 *                 ComposableFieldValue.SimpleText,
 *                 ComposableFieldValue.LongText
 *             )
 *         )
 *     }
 *     MyCard(header = header)
 * }
 * ```
 *
 * @param label The display label for this field
 * @param initialValue The initial ComposableFieldValue to display
 * @param choices List of available ComposableFieldValue options to choose from
 */
open class ComposableField(
    label: String,
    initialValue: ComposableFieldValue,
    val choices: List<ComposableFieldValue> = ComposableFieldValue.DefaultChoices,
) : ImmutablePreviewLabField<@Composable () -> Unit>(
    label = label,
    initialValue = { },
) {
    private var fieldValue by mutableStateOf(initialValue)
    override fun testValues(): List<@Composable (() -> Unit)> = super.testValues() + choices.map { { it.invoke() } }

    override var value: @Composable () -> Unit
        get() = { fieldValue() }
        set(_) {
            error("ComposableField.setValue not supported.")
        }

    @Composable
    override fun Content() {
        SelectButton(
            value = fieldValue,
            choices = choices,
            onSelect = { fieldValue = it },
            title = { it.label },
        )
    }
}

/**
 * Interface for values that can be used in a ComposableField.
 * Provides a label and composable content.
 *
 * # Usage
 *
 * ```kt
 * // Using predefined values
 * @Preview
 * @Composable
 * fun MyPreview() = PreviewLab {
 *     val content: @Composable () -> Unit = fieldValue {
 *         ComposableField(
 *             label = "Content",
 *             initialValue = ComposableFieldValue.Red64X64,
 *             choices = listOf(
 *                 ComposableFieldValue.Red32X32,
 *                 ComposableFieldValue.Red64X64,
 *                 ComposableFieldValue.SimpleText,
 *                 ComposableFieldValue.Empty
 *             )
 *         )
 *     }
 *     MyBox(content = content)
 * }
 *
 * // Creating custom ComposableFieldValue
 * val CustomIcon: ComposableFieldValue = object : ComposableFieldValue {
 *     override val label: String = "Custom Icon"
 *
 *     @Composable
 *     override fun invoke() {
 *         Icon(Icons.Default.Star, contentDescription = null)
 *     }
 * }
 *
 * // Using in a preview
 * @Preview
 * @Composable
 * fun CustomPreview() = PreviewLab {
 *     val icon: @Composable () -> Unit = fieldValue {
 *         ComposableField(
 *             label = "Icon",
 *             initialValue = CustomIcon
 *         )
 *     }
 *     MyButton(icon = icon)
 * }
 * ```
 *
 * @see ComposableField
 */
interface ComposableFieldValue {
    /** Display label for this composable value */
    val label: String

    /** Renders the composable content */
    @Composable
    operator fun invoke()

    /**
     * An empty ComposableFieldValue that renders nothing.
     */
    object Empty : ComposableFieldValue {
        override val label: String = "Empty"

        @Composable
        override operator fun invoke() {
            // nothing
        }
    }

    /**
     * A ComposableFieldValue that renders a colored box with configurable dimensions.
     * Supports both fixed dimensions and fill options.
     *
     * # Usage
     *
     * ```kt
     * // Fixed dimensions
     * @Preview
     * @Composable
     * fun ColorBoxPreview() = PreviewLab {
     *     val box: @Composable () -> Unit = fieldValue {
     *         ComposableField(
     *             label = "Box",
     *             initialValue = ComposableFieldValue.ColorBox(Color.Blue, 50.dp, 50.dp),
     *             choices = listOf(
     *                 ComposableFieldValue.ColorBox(Color.Red, 32.dp, 32.dp),
     *                 ComposableFieldValue.ColorBox(Color.Blue, 50.dp, 50.dp),
     *                 ComposableFieldValue.ColorBox(Color.Green, 64.dp, 64.dp)
     *             )
     *         )
     *     }
     *     MyContainer(content = box)
     * }
     *
     * // Fill dimensions
     * @Preview
     * @Composable
     * fun FillBoxPreview() = PreviewLab {
     *     val box: @Composable () -> Unit = fieldValue {
     *         ComposableField(
     *             label = "Background",
     *             initialValue = ComposableFieldValue.ColorBox(
     *                 Color.Gray,
     *                 ComposableFieldValue.ColorBox.Fill,
     *                 ComposableFieldValue.ColorBox.Fill
     *             ),
     *             choices = listOf(
     *                 ComposableFieldValue.ColorBox(Color.Red, ComposableFieldValue.ColorBox.Fill, 100.dp),
     *                 ComposableFieldValue.ColorBox(Color.Blue, 200.dp, ComposableFieldValue.ColorBox.Fill),
     *                 ComposableFieldValue.ColorBox(Color.Green, ComposableFieldValue.ColorBox.Fill, ComposableFieldValue.ColorBox.Fill)
     *             )
     *         )
     *     }
     *     MyCard(background = box)
     * }
     *
     * // With custom label
     * @Preview
     * @Composable
     * fun LabeledBoxPreview() = PreviewLab {
     *     val divider: @Composable () -> Unit = fieldValue {
     *         ComposableField(
     *             label = "Divider",
     *             initialValue = ComposableFieldValue.ColorBox(
     *                 Color.Gray,
     *                 ComposableFieldValue.ColorBox.Fill,
     *                 1.dp,
     *                 label = "Thin Divider"
     *             )
     *         )
     *     }
     *     Column {
     *         Text("Above")
     *         divider()
     *         Text("Below")
     *     }
     * }
     * ```
     *
     * @see ComposableField
     */
    class ColorBox private constructor(
        private val color: Color,
        private val widthOrFill: Dp?,
        private val heightOrFill: Dp?,
        label: String?,
    ) : ComposableFieldValue {
        override val label: String = label ?: "ColorBox(${widthOrFill ?: "fill"} x ${heightOrFill ?: "fill"})"

        constructor(color: Color, width: Dp, height: Dp, label: String? = null) : this(
            color = color,
            widthOrFill = width,
            heightOrFill = height,
            label = label,
        )

        constructor(color: Color, width: Fill, height: Dp, label: String? = null) : this(
            color = color,
            widthOrFill = _fill,
            heightOrFill = height,
            label = label,
        )

        constructor(color: Color, width: Dp, height: Fill, label: String? = null) : this(
            color = color,
            widthOrFill = width,
            heightOrFill = _fill,
            label = label,
        )

        constructor(color: Color, width: Fill, height: Fill, label: String? = null) : this(
            color = color,
            widthOrFill = _fill,
            heightOrFill = _fill,
            label = label,
        )

        fun copy(color: Color = this.color, label: String? = this.label) = ColorBox(
            color = color,
            label = label,
            widthOrFill = widthOrFill,
            heightOrFill = heightOrFill,
        )

        @Composable
        override operator fun invoke() {
            Box(
                modifier = Modifier
                    .background(color = color)
                    .thenIf(widthOrFill == _fill) { fillMaxWidth() }
                    .thenIfNotNull(widthOrFill) { width -> width(width) }
                    .thenIf(heightOrFill == _fill) { fillMaxHeight() }
                    .thenIfNotNull(heightOrFill) { height -> height(height) },
            )
        }

        object Fill

        companion object {
            @Suppress("ktlint:standard:backing-property-naming")
            private val _fill: Dp? = null
        }
    }

    /**
     * A ComposableFieldValue that renders text with optional font scaling and styling.
     *
     * # Usage
     *
     * ```kt
     * // Basic text content
     * @Preview
     * @Composable
     * fun TextPreview() = PreviewLab {
     *     val content: @Composable () -> Unit = fieldValue {
     *         ComposableField(
     *             label = "Content",
     *             initialValue = ComposableFieldValue.Text("Hello World"),
     *             choices = listOf(
     *                 ComposableFieldValue.Text("Short text"),
     *                 ComposableFieldValue.Text("Medium length text here"),
     *                 ComposableFieldValue.Text("Very long text that might wrap to multiple lines")
     *             )
     *         )
     *     }
     *     MyTextContainer(content = content)
     * }
     *
     * // With font scaling
     * @Preview
     * @Composable
     * fun ScaledTextPreview() = PreviewLab {
     *     val label: @Composable () -> Unit = fieldValue {
     *         ComposableField(
     *             label = "Label",
     *             initialValue = ComposableFieldValue.Text("Normal", fontScale = 1.0f),
     *             choices = listOf(
     *                 ComposableFieldValue.Text("Small", fontScale = 0.8f, label = "Small (0.8x)"),
     *                 ComposableFieldValue.Text("Normal", fontScale = 1.0f, label = "Normal (1.0x)"),
     *                 ComposableFieldValue.Text("Large", fontScale = 1.5f, label = "Large (1.5x)")
     *             )
     *         )
     *     }
     *     MyCard(label = label)
     * }
     *
     * // Using predefined text values
     * @Preview
     * @Composable
     * fun PredefinedTextPreview() = PreviewLab {
     *     val content: @Composable () -> Unit = fieldValue {
     *         ComposableField(
     *             label = "Content",
     *             initialValue = ComposableFieldValue.SimpleText,
     *             choices = listOf(
     *                 ComposableFieldValue.ShortText,
     *                 ComposableFieldValue.SimpleText,
     *                 ComposableFieldValue.HeadingText,
     *                 ComposableFieldValue.BodyText,
     *                 ComposableFieldValue.LongText
     *             )
     *         )
     *     }
     *     MyArticle(content = content)
     * }
     *
     * @param text The text content to display
     * @param fontScale Scale factor for the font size (default: 1f)
     * @param label Optional custom label (defaults to generated label)
     * @param textStyle Optional text style to apply
     * @see ComposableField
     */
    class Text(val text: String, val fontScale: Float = 1f, label: String? = null, private val textStyle: TextStyle? = null) :
        ComposableFieldValue {
        override val label: String =
            label ?: buildString {
                append("Text(")
                append("\"")
                append(text.replace("\"", ""))
                append("\"")
                if (fontScale != 1f) {
                    append(", ")
                    append("fontScale = $fontScale")
                }
                append(")")
            }

        @Composable
        override operator fun invoke() {
            CompositionLocalProvider(
                LocalDensity provides Density(LocalDensity.current.density, fontScale = fontScale),
            ) {
                me.tbsten.compose.preview.lab.ui.components.Text(
                    text = text,
                    style = textStyle ?: LocalTextStyle.current,
                )
            }
        }
    }

    companion object {
        val Red20X20 = ColorBox(Color.Red, 20.dp, 20.dp)
        val Red32X32 = ColorBox(Color.Red, 32.dp, 32.dp)
        val Red64X64 = ColorBox(Color.Red, 64.dp, 64.dp)
        val Red180X80 = ColorBox(Color.Red, 180.dp, 80.dp)
        val Red300X300 = ColorBox(Color.Red, 300.dp, 300.dp)
        val RedFillX80 = ColorBox(Color.Red, ColorBox.Fill, 80.dp)
        val RedFillX300 = ColorBox(Color.Red, ColorBox.Fill, 300.dp)
        val Red80XFill = ColorBox(Color.Red, 80.dp, ColorBox.Fill)
        val Red300XFill = ColorBox(Color.Red, 300.dp, ColorBox.Fill)
        val RedFillXFill = ColorBox(Color.Red, ColorBox.Fill, ColorBox.Fill)

        val LongText = Text(text = "Very ${"long".repeat(100)} text", label = "Long text")
        val HeadingText = Text(text = "Hello Compose Preview Lab !", label = "HeadingText")
        val BodyText = Text(
            text = """
            Compose Preview Lab turns @Preview into an interactive Component Playground.
            You can pass parameters to components, enabling more than just static snapshots—making manual testing easier and helping new developers understand components faster.
            Compose Multiplatform is supported.
            """.trimIndent(),
            label = "BodyText",
        )
        val SimpleText = Text(text = "Simple text", label = "Simple text")
        val ShortText = Text(text = "S", label = "Short text")
        val BigScaledText = Text(text = "Big scaled text", fontScale = 2.0f, label = "Big scaled text")
        val SmallScaledText = Text(text = "Small scaled text", fontScale = 0.8f, label = "Small scaled text")

        val DefaultChoices: List<ComposableFieldValue> = listOf(
            Red20X20,
            Red32X32,
            Red64X64,
            Red180X80,
            Red300X300,
            RedFillX80,
            RedFillX300,
            Red80XFill,
            Red300XFill,
            RedFillXFill,
            LongText,
            HeadingText,
            BodyText,
            SimpleText,
            ShortText,
            BigScaledText,
            SmallScaledText,
            Empty,
        )
    }
}

/**
 * Creates a ComposableFieldValue with custom content.
 *
 * # Usage
 *
 * ```kt
 * // Simple custom content
 * @Preview
 * @Composable
 * fun CustomContentPreview() = PreviewLab {
 *     val content: @Composable () -> Unit = fieldValue {
 *         ComposableField(
 *             label = "Content",
 *             initialValue = ComposableFieldValue("Icon") {
 *                 Icon(Icons.Default.Star, contentDescription = null)
 *             },
 *             choices = listOf(
 *                 ComposableFieldValue("Star") { Icon(Icons.Default.Star, null) },
 *                 ComposableFieldValue("Heart") { Icon(Icons.Default.Favorite, null) },
 *                 ComposableFieldValue("Home") { Icon(Icons.Default.Home, null) }
 *             )
 *         )
 *     }
 *     MyButton(icon = content)
 * }
 *
 * // Complex custom content
 * @Preview
 * @Composable
 * fun ComplexContentPreview() = PreviewLab {
 *     val slot: @Composable () -> Unit = fieldValue {
 *         ComposableField(
 *             label = "Slot Content",
 *             initialValue = ComposableFieldValue("Image + Text") {
 *                 Row {
 *                     Icon(Icons.Default.Image, null)
 *                     Text("With Image")
 *                 }
 *             },
 *             choices = listOf(
 *                 ComposableFieldValue("Text Only") { Text("Simple Text") },
 *                 ComposableFieldValue("Image + Text") {
 *                     Row {
 *                         Icon(Icons.Default.Image, null)
 *                         Text("With Image")
 *                     }
 *                 },
 *                 ComposableFieldValue("Complex") {
 *                     Column {
 *                         Text("Title", style = MaterialTheme.typography.h6)
 *                         Text("Subtitle", style = MaterialTheme.typography.body2)
 *                     }
 *                 }
 *             )
 *         )
 *     }
 *     MyCard(content = slot)
 * }
 *
 * // Mixing with predefined values
 * @Preview
 * @Composable
 * fun MixedContentPreview() = PreviewLab {
 *     val content: @Composable () -> Unit = fieldValue {
 *         ComposableField(
 *             label = "Content",
 *             initialValue = ComposableFieldValue.Empty,
 *             choices = listOf(
 *                 ComposableFieldValue.Empty,
 *                 ComposableFieldValue("Custom Icon") { Icon(Icons.Default.Settings, null) },
 *                 ComposableFieldValue.Red32X32,
 *                 ComposableFieldValue.SimpleText
 *             )
 *         )
 *     }
 *     MyContainer(trailing = content)
 * }
 * ```
 *
 * @param label The display label for this value
 * @param content The composable content to render
 * @return A ComposableFieldValue that renders the provided content
 * @see ComposableField
 */
@Suppress("ktlint:standard:function-naming")
fun ComposableFieldValue(label: String, content: @Composable () -> Unit) = object : ComposableFieldValue {
    override val label: String = label

    @Composable
    override fun invoke() {
        content()
    }
}

/**
 * Converts a [PreviewParameterProvider] to a [SelectableField].
 *
 * # Usage
 *
 * ```kt
 * class MyButtonProperty(val text: String, val backgroundColor: Color, val contentColor: Color)
 * class MyButtonPreviewParameterProvider() : PreviewParameterProvider<MyButtonProperty> {
 *   override val values: Sequence<MyButtonProperty> = sequenceOf(...)
 * }
 *
 * @Preview
 * @Composable
 * private fun MyButtonPreview() = PreviewLab {
 *   val properties = fieldValue {
 *     MyButtonPreviewParameterProvider().toField("properties")
 *   }
 *
 *   MyButton(
 *     text = properties.text,
 *     backgroundColor = properties.backgroundColor,
 *     contentColor = properties.contentColor,
 *   )
 * }
 * ```
 *
 * @see ComposableField
 */
fun <Value> PreviewParameterProvider<Value>.toField(
    label: String,
    choiceLabel: (Value) -> String = { it.toString() },
    type: Type = DROPDOWN,
): SelectableField<Value> = SelectableField(
    label = label,
    choices = this.values.toList(),
    choiceLabel = choiceLabel,
    type = type,
)
