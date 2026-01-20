package me.tbsten.compose.preview.lab.field.modifier

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.DpTransformer
import me.tbsten.compose.preview.lab.ui.components.NullableDpTransformer
import me.tbsten.compose.preview.lab.ui.components.Text
import me.tbsten.compose.preview.lab.ui.components.TransformableTextField
import me.tbsten.compose.preview.lab.ui.components.colorpicker.CommonColorPicker

/**
 * A modifier field value that applies a border to a component.
 *
 * @param color The border color
 * @param width The border width
 */
public class BorderModifierFieldValue(color: Color, width: Dp) : ModifierFieldValue {
    public var color: Color by mutableStateOf(color)
    public var width: Dp by mutableStateOf(width)

    override fun Modifier.createModifier(): Modifier = border(
        color = color,
        width = width,
        // TODO shape
    )

    @Composable
    override fun Builder(): Unit = DefaultModifierFieldValueBuilder(
        modifierTextCode = buildAnnotatedString {
            appendLine(".border(")

            append("  color = ")
            appendInlineContent(id = "COLOR_PREVIEW")
            append("Color(0x")
            listOf(color.alpha, color.red, color.green, color.blue).forEach { colorValue ->
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append((colorValue * 255).roundToInt().toString(16).padStart(2, '0').uppercase())
                }
            }
            append("),")
            appendLine()

            append("  width = ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("${width.value}")
            }
            append(".dp,")
            appendLine()

            append(")")
        },
        inlineContent = mapOf(
            "COLOR_PREVIEW" to InlineTextContent(
                placeholder = Placeholder((12 + 2).sp, 12.sp, PlaceholderVerticalAlign.Center),
                children = {
                    with(LocalDensity.current) {
                        Box(
                            modifier = Modifier
                                .padding(end = 2.sp.toDp())
                                .border(0.5.dp, color, shape = RoundedCornerShape(5.dp))
                                .padding(1.dp)
                                .background(color, shape = RoundedCornerShape(4.dp))
                                .size(12.sp.toDp()),
                        )
                    }
                },
            ),
        ),
        menuContent = {
            DefaultMenu {
                ColorPickerItem(
                    label = "color",
                    value = color,
                    onValueChange = { color = it },
                )
                TextFieldItem(
                    label = "width",
                    value = width,
                    onValueChange = { width = it },
                    transformer = DpTransformer,
                    suffix = ".dp",
                )
            }
        },
    )

    /**
     * Factory for creating BorderModifierFieldValue instances with configurable initial values.
     *
     * @param initialColor Initial border color to apply
     * @param initialWidth Initial border width to apply
     */
    public class Factory(initialColor: Color? = null, initialWidth: Dp? = null) :
        ModifierFieldValueFactory<BorderModifierFieldValue> {
        override val title: String = ".border(...)"
        public var color: Color? by mutableStateOf(initialColor)
        public var width: Dp? by mutableStateOf(initialWidth)

        override val canCreate: Boolean
            get() =
                color != null && width != null

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)): Unit = Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CommonColorPicker(
                color = color ?: Color.Unspecified,
                onColorSelected = { color = it },
            )

            TransformableTextField(
                value = width,
                onValueChange = { width = it },
                transformer = NullableDpTransformer,
                textStyle = PreviewLabTheme.typography.label1,
                prefix = { Text("width: ", style = PreviewLabTheme.typography.label2) },
            )

            Row { createButton() }
        }

        override fun create(): Result<BorderModifierFieldValue> = runCatching {
            BorderModifierFieldValue(
                color = requireNotNull(color) { "color is null" },
                width = requireNotNull(width) { "width is null" },
            )
        }
    }
}

/**
 * Adds a border to this modifier list with specified color and width.
 *
 * @param color The color of the border
 * @param width The width of the border
 * @return A new ModifierFieldValueList with border applied
 */
public fun ModifierFieldValueList.border(color: Color, width: Dp): ModifierFieldValueList = then(
    BorderModifierFieldValue(
        color = color,
        width = width,
    ),
)
