package me.tbsten.compose.preview.lab.field.modifier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
 * Modifier field value for controlling component transparency
 *
 * Applies alpha blending to make components partially or fully transparent.
 * Essential for creating overlay effects, fade animations, and visual hierarchy.
 * Provides an interactive slider or text input for precise alpha control.
 *
 * ```kotlin
 * // Semi-transparent overlay
 * val overlay = ModifierFieldValueList().alpha(0.7f)
 *
 * // Fade effect
 * val fadeOut = ModifierFieldValueList().alpha(0.3f)
 *
 * // Use in component
 * Box(
 *     modifier = overlay.createModifier()
 * ) {
 *     Text("Transparent content")
 * }
 * ```
 *
 * @param alpha Transparency level from 0.0 (fully transparent) to 1.0 (fully opaque)
 * @see ModifierFieldValue
 * @see alpha
 */
class AlphaModifierFieldValue(alpha: Float) : ModifierFieldValue {
    var alpha by mutableStateOf(alpha)

    override fun Modifier.createModifier(): Modifier = alpha(
        alpha = alpha,
    )

    @Composable
    override fun Builder() = DefaultModifierFieldValueBuilder(
        modifierTextCode = buildAnnotatedString {
            appendLine(".alpha(")

            append("  alpha = ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("$alpha")
            }
            appendLine("f,")

            append(")")
        },
        menuContent = {
            DefaultMenu {
                TextFieldItem(
                    label = "alpha",
                    value = alpha,
                    onValueChange = { alpha = it },
                    transformer = FloatTransformer,
                )
            }
        },
    )

    /**
     * Factory for creating AlphaModifierFieldValue instances with configurable initial value
     *
     * Provides UI for setting the alpha value before creating the modifier field value.
     * Includes validation to ensure alpha is set before creation.
     *
     * @param initialAlpha Starting alpha value (optional)
     */
    class Factory(initialAlpha: Float? = null) : ModifierFieldValueFactory<AlphaModifierFieldValue> {
        override val title: String = ".alpha(...)"
        var alpha by mutableStateOf(initialAlpha)

        override val canCreate: Boolean
            get() = alpha != null

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)) = Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TransformableTextField(
                value = alpha,
                onValueChange = { alpha = it },
                transformer = NullableFloatTransformer,
                textStyle = PreviewLabTheme.typography.label1,
                prefix = { Text("alpha: ", style = PreviewLabTheme.typography.label2) },
            )

            Row { createButton() }
        }

        override fun create(): Result<AlphaModifierFieldValue> = runCatching {
            AlphaModifierFieldValue(
                alpha = requireNotNull(alpha) { "alpha is null" },
            )
        }
    }
}

/**
 * Adds alpha/transparency to this modifier list.
 *
 * @param alpha The alpha value from 0.0 (transparent) to 1.0 (opaque)
 * @return A new ModifierFieldValueList with alpha applied
 */
fun ModifierFieldValueList.alpha(alpha: Float) = then(
    AlphaModifierFieldValue(
        alpha = alpha,
    ),
)
