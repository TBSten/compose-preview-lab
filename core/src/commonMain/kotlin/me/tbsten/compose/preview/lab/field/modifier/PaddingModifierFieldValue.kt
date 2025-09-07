package me.tbsten.compose.preview.lab.field.modifier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.component.DpTransformer
import me.tbsten.compose.preview.lab.component.NullableDpTransformer
import me.tbsten.compose.preview.lab.component.TransformableTextField
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Text

/**
 * A modifier field value that applies padding to a component.
 *
 * Allows configuring padding values for all four sides (start, end, top, bottom)
 * of a component through the Preview Lab interface.
 *
 * @param start The start padding value
 * @param end The end padding value
 * @param top The top padding value
 * @param bottom The bottom padding value
 */
class PaddingModifierFieldValue(start: Dp, end: Dp, top: Dp, bottom: Dp) : ModifierFieldValue {
    var start by mutableStateOf(start)
    var end by mutableStateOf(end)
    var top by mutableStateOf(top)
    var bottom by mutableStateOf(bottom)

    override fun Modifier.createModifier(): Modifier = padding(
        start = start,
        end = end,
        top = top,
        bottom = bottom,
    )

    @Composable
    override fun Builder() = DefaultModifierFieldValueBuilder(
        modifierTextCode = buildAnnotatedString {
            appendLine(".padding(")

            append("  ")
            appendDp("start", start)
            append(", ")
            appendDp("end", end)
            append(", ")
            appendLine()

            append("  ")
            appendDp("top", top)
            append(", ")
            appendDp("bottom", bottom)
            appendLine()

            append(")")
        },
        menuContent = {
            DefaultMenu {
                TextFieldItem(
                    label = "start",
                    value = start,
                    onValueChange = { start = it },
                    transformer = DpTransformer,
                    suffix = ".dp",
                )
                TextFieldItem(
                    label = "end",
                    value = end,
                    onValueChange = { end = it },
                    transformer = DpTransformer,
                    suffix = ".dp",
                )
                TextFieldItem(
                    label = "top",
                    value = top,
                    onValueChange = { top = it },
                    transformer = DpTransformer,
                    suffix = ".dp",
                )
                TextFieldItem(
                    label = "bottom",
                    value = bottom,
                    onValueChange = { bottom = it },
                    transformer = DpTransformer,
                    suffix = ".dp",
                )
            }
        },
    )

    /**
     * Factory for creating PaddingModifierFieldValue instances with configurable initial values.
     *
     * @param initialStart Initial start padding value
     * @param initialEnd Initial end padding value
     * @param initialTop Initial top padding value
     * @param initialBottom Initial bottom padding value
     */
    open class Factory(
        private val initialStart: Dp? = null,
        private val initialEnd: Dp? = null,
        private val initialTop: Dp? = null,
        private val initialBottom: Dp? = null,
    ) : ModifierFieldValueFactory<PaddingModifierFieldValue> {
        override val title: String = ".padding(...)"

        var start: Dp? by mutableStateOf(initialStart)
        var end: Dp? by mutableStateOf(initialEnd)
        var top: Dp? by mutableStateOf(initialTop)
        var bottom: Dp? by mutableStateOf(initialBottom)

        override val canCreate: Boolean
            get() =
                start != null &&
                    end != null &&
                    top != null &&
                    bottom != null

        constructor(initialAll: Dp? = null) : this(
            initialStart = initialAll,
            initialEnd = initialAll,
            initialTop = initialAll,
            initialBottom = initialAll,
        )

        constructor(initialHorizontal: Dp? = null, initialVertical: Dp? = null) : this(
            initialStart = initialHorizontal,
            initialEnd = initialHorizontal,
            initialTop = initialVertical,
            initialBottom = initialVertical,
        )

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)) = Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TransformableTextField(
                value = start,
                onValueChange = { start = it },
                transformer = NullableDpTransformer,
                textStyle = PreviewLabTheme.typography.label1,
                prefix = { Text("start: ", style = PreviewLabTheme.typography.label2) },
            )

            TransformableTextField(
                value = end,
                onValueChange = { end = it },
                transformer = NullableDpTransformer,
                textStyle = PreviewLabTheme.typography.label1,
                prefix = { Text("end: ", style = PreviewLabTheme.typography.label2) },
            )

            TransformableTextField(
                value = top,
                onValueChange = { top = it },
                transformer = NullableDpTransformer,
                textStyle = PreviewLabTheme.typography.label1,
                prefix = { Text("top: ", style = PreviewLabTheme.typography.label2) },
            )

            TransformableTextField(
                value = bottom,
                onValueChange = { bottom = it },
                transformer = NullableDpTransformer,
                textStyle = PreviewLabTheme.typography.label1,
                prefix = { Text("bottom: ", style = PreviewLabTheme.typography.label2) },
            )

            Row { createButton() }
        }

        override fun create(): Result<PaddingModifierFieldValue> = runCatching {
            PaddingModifierFieldValue(
                start = requireNotNull(start) { "start is null" },
                end = requireNotNull(end) { "end is null" },
                top = requireNotNull(top) { "top is null" },
                bottom = requireNotNull(bottom) { "bottom is null" },
            )
        }
    }
}

private fun AnnotatedString.Builder.appendDp(argName: String, dp: Dp) {
    append("$argName = ")
    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
        append("${dp.value}")
    }
    append(".dp")
}

/**
 * Adds padding to this modifier list with individual side values.
 *
 * @param start The start padding
 * @param end The end padding
 * @param top The top padding
 * @param bottom The bottom padding
 * @return A new ModifierFieldValueList with padding applied
 */
fun ModifierFieldValueList.padding(start: Dp = 0.dp, end: Dp = 0.dp, top: Dp = 0.dp, bottom: Dp = 0.dp) = then(
    PaddingModifierFieldValue(
        start = start,
        end = end,
        top = top,
        bottom = bottom,
    ),
)

/**
 * Adds padding to this modifier list with horizontal and vertical values.
 *
 * @param horizontal The horizontal padding (applied to start and end)
 * @param vertical The vertical padding (applied to top and bottom)
 * @return A new ModifierFieldValueList with padding applied
 */
fun ModifierFieldValueList.padding(horizontal: Dp = 0.dp, vertical: Dp = 0.dp) = then(
    PaddingModifierFieldValue(
        start = horizontal,
        end = horizontal,
        top = vertical,
        bottom = vertical,
    ),
)

/**
 * Adds equal padding to all sides of this modifier list.
 *
 * @param all The padding value to apply to all sides
 * @return A new ModifierFieldValueList with padding applied
 */
fun ModifierFieldValueList.padding(all: Dp) = then(
    PaddingModifierFieldValue(
        start = all,
        end = all,
        top = all,
        bottom = all,
    ),
)
