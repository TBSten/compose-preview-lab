package me.tbsten.compose.preview.lab.field.modifier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.FloatTransformer
import me.tbsten.compose.preview.lab.ui.components.NullableFloatTransformer
import me.tbsten.compose.preview.lab.ui.components.Text
import me.tbsten.compose.preview.lab.ui.components.TransformableTextField

/**
 * A modifier field value that scales a component.
 *
 * @param scaleX The scale factor for the X axis
 * @param scaleY The scale factor for the Y axis
 */
class ScaleModifierFieldValue(scaleX: Float, scaleY: Float) : ModifierFieldValue {
    var scaleX by mutableStateOf(scaleX)
    var scaleY by mutableStateOf(scaleY)

    override fun Modifier.createModifier(): Modifier = scale(
        scaleX = scaleX,
        scaleY = scaleY,
    )

    @Composable
    override fun Builder() = DefaultModifierFieldValueBuilder(
        modifierTextCode = buildAnnotatedString {
            appendLine(".scale(")

            append("  scaleX = ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("$scaleX")
            }
            appendLine("f,")

            append("  scaleY = ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("$scaleY")
            }
            appendLine("f,")

            append(")")
        },
        menuContent = {
            DefaultMenu {
                TextFieldItem(
                    label = "scaleX",
                    value = scaleX,
                    onValueChange = { scaleX = it },
                    transformer = FloatTransformer,
                )

                TextFieldItem(
                    label = "scaleY",
                    value = scaleY,
                    onValueChange = { scaleY = it },
                    transformer = FloatTransformer,
                )
            }
        },
    )

    class Factory(initialScaleX: Float? = null, initialScaleY: Float? = null) :
        ModifierFieldValueFactory<ScaleModifierFieldValue> {
        override val title: String = ".scale(...)"
        var scaleX by mutableStateOf(initialScaleX)
        var scaleY by mutableStateOf(initialScaleY)

        override val canCreate: Boolean
            get() = scaleX != null && scaleY != null

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)) = Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TransformableTextField(
                value = scaleX,
                onValueChange = { scaleX = it },
                transformer = NullableFloatTransformer,
                textStyle = PreviewLabTheme.typography.label1,
                prefix = { Text("scaleX: ", style = PreviewLabTheme.typography.label2) },
            )

            TransformableTextField(
                value = scaleY,
                onValueChange = { scaleY = it },
                transformer = NullableFloatTransformer,
                textStyle = PreviewLabTheme.typography.label1,
                prefix = { Text("scaleY: ", style = PreviewLabTheme.typography.label2) },
            )

            Row { createButton() }
        }

        override fun create(): Result<ScaleModifierFieldValue> = runCatching {
            ScaleModifierFieldValue(
                scaleX = requireNotNull(scaleX) { "scaleX is null" },
                scaleY = requireNotNull(scaleY) { "scaleY is null" },
            )
        }
    }
}

/**
 * Scales this modifier list with separate X and Y scale factors.
 *
 * @param scaleX The horizontal scale factor
 * @param scaleY The vertical scale factor
 * @return A new ModifierFieldValueList with scaling applied
 */
fun ModifierFieldValueList.scale(scaleX: Float, scaleY: Float) = then(
    ScaleModifierFieldValue(
        scaleX = scaleX,
        scaleY = scaleY,
    ),
)

/**
 * Scales this modifier list uniformly in both X and Y directions.
 *
 * @param scale The scale factor to apply to both X and Y dimensions
 * @return A new ModifierFieldValueList with uniform scaling applied
 */
fun ModifierFieldValueList.scale(scale: Float) = then(
    ScaleModifierFieldValue(
        scaleX = scale,
        scaleY = scale,
    ),
)
