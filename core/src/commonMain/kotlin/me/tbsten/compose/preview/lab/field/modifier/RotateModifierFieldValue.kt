package me.tbsten.compose.preview.lab.field.modifier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.component.FloatTransformer
import me.tbsten.compose.preview.lab.component.NullableFloatTransformer
import me.tbsten.compose.preview.lab.component.TransformableTextField
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Text

/**
 * ModifierFieldValue that applies rotation transformation to a composable.
 *
 * @param degrees The rotation angle in degrees. Positive values rotate clockwise.
 */
class RotateModifierFieldValue(degrees: Float) : ModifierFieldValue {
    var degrees by mutableStateOf(degrees)

    override fun Modifier.createModifier(): Modifier = rotate(
        degrees = degrees,
    )

    @Composable
    override fun Builder() = DefaultModifierFieldValueBuilder(
        modifierTextCode = buildAnnotatedString {
            appendLine(".rotate(")

            append("  degrees = ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("$degrees")
            }
            appendLine("f,")

            append(")")
        },
        menuContent = {
            DefaultMenu {
                TextFieldItem(
                    label = "degrees",
                    value = degrees,
                    onValueChange = { degrees = it },
                    transformer = FloatTransformer,
                )
            }
        },
    )

    /**
     * Factory for creating RotateModifierFieldValue instances with configurable initial values.
     *
     * @param initialDegrees Initial rotation angle in degrees (positive values rotate clockwise)
     */
    class Factory(initialDegrees: Float? = null) : ModifierFieldValueFactory<RotateModifierFieldValue> {
        override val title: String = ".rotate(...)"
        var degrees by mutableStateOf(initialDegrees)

        override val canCreate: Boolean
            get() = degrees != null

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)) = Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TransformableTextField(
                value = degrees,
                onValueChange = { degrees = it },
                transformer = NullableFloatTransformer,
                textStyle = PreviewLabTheme.typography.label1,
                prefix = { Text("degrees: ", style = PreviewLabTheme.typography.label2) },
            )

            Row { createButton() }
        }

        override fun create(): Result<RotateModifierFieldValue> = runCatching {
            RotateModifierFieldValue(
                degrees = requireNotNull(degrees) { "degrees is null" },
            )
        }
    }
}

/**
 * Rotates this modifier list by the specified degrees.
 *
 * @param degrees The rotation angle in degrees
 * @return A new ModifierFieldValueList with rotation applied
 */
fun ModifierFieldValueList.rotate(degrees: Float) = then(
    RotateModifierFieldValue(
        degrees = degrees,
    ),
)
