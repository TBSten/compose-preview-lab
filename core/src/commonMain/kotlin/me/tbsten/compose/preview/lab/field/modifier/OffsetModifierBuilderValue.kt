package me.tbsten.compose.preview.lab.field.modifier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
 * ModifierFieldValue that applies positional offset to a composable.
 *
 * @param x The horizontal offset in Dp. Positive values move to the right.
 * @param y The vertical offset in Dp. Positive values move downward.
 */
class OffsetModifierFieldValue(x: Dp, y: Dp) : ModifierFieldValue {
    var x by mutableStateOf(x)
    var y by mutableStateOf(y)

    override fun Modifier.createModifier(): Modifier = offset(
        x = x,
        y = y,
    )

    @Composable
    override fun Builder() = DefaultModifierFieldValueBuilder(
        modifierTextCode = buildAnnotatedString {
            appendLine(".offset(")

            append("  x = ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("${x.value}")
            }
            append(".dp,")
            appendLine()

            append("  y = ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("${y.value}")
            }
            append(".dp,")
            appendLine()

            append(")")
        },
        menuContent = {
            DefaultMenu {
                TextFieldItem(
                    label = "x",
                    value = x,
                    onValueChange = { x = it },
                    transformer = DpTransformer,
                    suffix = ".dp",
                )
                TextFieldItem(
                    label = "y",
                    value = y,
                    onValueChange = { y = it },
                    transformer = DpTransformer,
                    suffix = ".dp",
                )
            }
        },
    )

    /**
     * Factory for creating OffsetModifierFieldValue instances with configurable initial values.
     *
     * @param initialX Initial horizontal offset value
     * @param initialY Initial vertical offset value
     */
    class Factory(initialX: Dp? = null, initialY: Dp? = null) : ModifierFieldValueFactory<OffsetModifierFieldValue> {
        override val title: String = ".offset(...)"
        var x by mutableStateOf(initialX)
        var y by mutableStateOf(initialY)

        override val canCreate: Boolean
            get() =
                x != null && y != null

        constructor(
            initialAll: Dp? = null,
        ) : this(
            initialX = initialAll,
            initialY = initialAll,
        )

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)) = Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TransformableTextField(
                value = x,
                onValueChange = { x = it },
                transformer = NullableDpTransformer,
                textStyle = PreviewLabTheme.typography.label1,
                prefix = { Text("x: ", style = PreviewLabTheme.typography.label2) },
            )

            TransformableTextField(
                value = y,
                onValueChange = { y = it },
                transformer = NullableDpTransformer,
                textStyle = PreviewLabTheme.typography.label1,
                prefix = { Text("y: ", style = PreviewLabTheme.typography.label2) },
            )

            Row { createButton() }
        }

        override fun create(): Result<OffsetModifierFieldValue> = runCatching {
            OffsetModifierFieldValue(
                x = requireNotNull(x) { "x is null" },
                y = requireNotNull(y) { "y is null" },
            )
        }
    }
}

/**
 * Offsets this modifier list by the specified X and Y coordinates.
 *
 * @param x The horizontal offset in Dp (defaults to 0.dp)
 * @param y The vertical offset in Dp (defaults to 0.dp)
 * @return A new ModifierFieldValueList with offset applied
 */
fun ModifierFieldValueList.offset(x: Dp = 0.dp, y: Dp = 0.dp) = then(
    OffsetModifierFieldValue(
        x = x,
        y = y,
    ),
)

/**
 * Offsets this modifier list uniformly in both X and Y directions.
 *
 * @param offset The offset value to apply to both X and Y coordinates (defaults to 0.dp)
 * @return A new ModifierFieldValueList with uniform offset applied
 */
fun ModifierFieldValueList.offset(offset: Dp = 0.dp) = then(
    OffsetModifierFieldValue(
        x = offset,
        y = offset,
    ),
)
