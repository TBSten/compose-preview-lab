package me.tbsten.compose.preview.lab.field.modifier

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
import me.tbsten.compose.preview.lab.component.FloatTransformer
import me.tbsten.compose.preview.lab.component.NullableFloatTransformer
import me.tbsten.compose.preview.lab.component.TransformableTextField
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Text

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

    class Factory(initialAlpha: Float? = null) : ModifierFieldValueFactory<AlphaModifierFieldValue> {
        override val title: String = ".alpha(...)"
        var alpha by mutableStateOf(initialAlpha)

        override val canCreate: Boolean
            get() = alpha != null

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)) = Column {
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

fun ModifierFieldValueList.alpha(alpha: Float) = then(
    AlphaModifierFieldValue(
        alpha = alpha,
    ),
)
