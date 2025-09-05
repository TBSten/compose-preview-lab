package me.tbsten.compose.preview.lab.field.modifier

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import me.tbsten.compose.preview.lab.component.FloatTransformer
import me.tbsten.compose.preview.lab.component.NullableFloatTransformer
import me.tbsten.compose.preview.lab.component.TransformableTextField
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Text

class AspectRatioModifierFieldValue(ratio: Float) : ModifierFieldValue {
    var ratio by mutableStateOf(ratio)

    override fun Modifier.createModifier(): Modifier = aspectRatio(ratio = ratio)

    @Composable
    override fun Builder() = DefaultModifierFieldValueBuilder(
        modifierTextCode = buildAnnotatedString {
            appendLine(".aspectRatio(")

            append("  ratio = ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("$ratio")
            }
            append("f,")
            appendLine()

            append(")")
        },
        menuContent = {
            DefaultMenu {
                TextFieldItem(
                    label = "ratio",
                    value = ratio,
                    onValueChange = { ratio = it },
                    transformer = FloatTransformer,
                )
            }
        },
    )

    class Factory(initialRatio: Float? = null) : ModifierFieldValueFactory<AspectRatioModifierFieldValue> {
        override val title: String = ".aspectRatio(...)"
        var ratio by mutableStateOf(initialRatio)

        override val canCreate: Boolean
            get() = ratio != null

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)) = Column {
            TransformableTextField(
                value = ratio,
                onValueChange = { ratio = it },
                transformer = NullableFloatTransformer,
                textStyle = PreviewLabTheme.typography.label1,
                prefix = { Text("ratio: ", style = PreviewLabTheme.typography.label2) },
            )

            Row { createButton() }
        }

        override fun create(): Result<AspectRatioModifierFieldValue> = runCatching {
            AspectRatioModifierFieldValue(
                ratio = requireNotNull(ratio) { "ratio is null" },
            )
        }
    }
}

fun ModifierFieldValueList.aspectRatio(ratio: Float) = then(
    AspectRatioModifierFieldValue(
        ratio = ratio,
    ),
)
