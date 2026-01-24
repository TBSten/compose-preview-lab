package me.tbsten.compose.preview.lab.field.modifier

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.PreviewLabFloatTransformer
import me.tbsten.compose.preview.lab.ui.components.PreviewLabNullableFloatTransformer
import me.tbsten.compose.preview.lab.ui.components.PreviewLabText
import me.tbsten.compose.preview.lab.ui.components.PreviewLabTransformableTextField

/**
 * ModifierFieldValue that applies an aspect ratio constraint to a composable.
 *
 * @param ratio The aspect ratio value (width/height). For example, 1.0f for square, 16/9f for widescreen.
 */
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
                    transformer = PreviewLabFloatTransformer,
                )
            }
        },
    )

    /**
     * Factory for creating AspectRatioModifierFieldValue instances with configurable initial values.
     *
     * @param initialRatio Initial aspect ratio value (width/height ratio)
     */
    class Factory(initialRatio: Float? = null) : ModifierFieldValueFactory<AspectRatioModifierFieldValue> {
        override val title: String = ".aspectRatio(...)"
        var ratio by mutableStateOf(initialRatio)

        override val canCreate: Boolean
            get() = ratio != null

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)) = Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PreviewLabTransformableTextField(
                value = ratio,
                onValueChange = { ratio = it },
                transformer = PreviewLabNullableFloatTransformer,
                textStyle = PreviewLabTheme.typography.label1,
                prefix = { PreviewLabText("ratio: ", style = PreviewLabTheme.typography.label2) },
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

/**
 * Sets the aspect ratio constraint for this modifier list.
 *
 * @param ratio The aspect ratio value (width/height)
 * @return A new ModifierFieldValueList with aspect ratio applied
 */
fun ModifierFieldValueList.aspectRatio(ratio: Float) = then(
    AspectRatioModifierFieldValue(
        ratio = ratio,
    ),
)
