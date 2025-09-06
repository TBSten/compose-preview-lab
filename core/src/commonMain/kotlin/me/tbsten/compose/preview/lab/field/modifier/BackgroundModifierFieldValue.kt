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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import me.tbsten.compose.preview.lab.component.colorpicker.CommonColorPicker

/**
 * A modifier field value that applies a background color to a component.
 *
 * Allows configuring the background color of a component through an interactive
 * color picker in the Preview Lab interface.
 *
 * @param color The background color to apply
 */
class BackgroundModifierFieldValue(color: Color) : ModifierFieldValue {
    var color by mutableStateOf(color)

    override fun Modifier.createModifier(): Modifier = background(
        color = color,
        // TODO shape
    )

    @Composable
    override fun Builder() = DefaultModifierFieldValueBuilder(
        modifierTextCode = buildAnnotatedString {
            appendLine(".background(")

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
            }
        },
    )

    /**
     * Factory for creating BackgroundModifierFieldValue instances with configurable initial values.
     *
     * @param initialColor Initial background color to apply
     */
    class Factory(initialColor: Color? = null) : ModifierFieldValueFactory<BackgroundModifierFieldValue> {
        override val title: String = ".background(...)"
        var color by mutableStateOf(initialColor)

        override val canCreate: Boolean
            get() = color != null

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)) = Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CommonColorPicker(
                color = color ?: Color.Unspecified,
                onColorSelected = { color = it },
            )

            Row { createButton() }
        }

        override fun create(): Result<BackgroundModifierFieldValue> = runCatching {
            BackgroundModifierFieldValue(
                color = requireNotNull(color) { "color is null" },
            )
        }
    }
}

/**
 * Adds a background color to this modifier list.
 *
 * @param color The background color to apply
 * @return A new ModifierFieldValueList with background applied
 */
fun ModifierFieldValueList.background(color: Color) = then(
    BackgroundModifierFieldValue(
        color = color,
    ),
)
