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
import me.tbsten.compose.preview.lab.component.SelectButton
import me.tbsten.compose.preview.lab.ui.LocalTextStyle
import me.tbsten.compose.preview.lab.util.thenIf
import me.tbsten.compose.preview.lab.util.thenIfNotNull

/**
 * A field that allows selecting from predefined Composable content options.
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
     * @param text The text content to display
     * @param fontScale Scale factor for the font size (default: 1f)
     * @param label Optional custom label (defaults to generated label)
     * @param textStyle Optional text style to apply
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
 * @param label The display label for this value
 * @param content The composable content to render
 * @return A ComposableFieldValue that renders the provided content
 */
@Suppress("ktlint:standard:function-naming")
fun ComposableFieldValue(label: String, content: @Composable () -> Unit) = object : ComposableFieldValue {
    override val label: String = label

    @Composable
    override fun invoke() {
        content()
    }
}
