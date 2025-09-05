package me.tbsten.compose.preview.lab.field.modifier

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.zIndex
import me.tbsten.compose.preview.lab.component.FloatTransformer
import me.tbsten.compose.preview.lab.component.NullableFloatTransformer
import me.tbsten.compose.preview.lab.component.TransformableTextField
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Text

class ZIndexModifierFieldValue(zIndex: Float) : ModifierFieldValue {
    var zIndex by mutableStateOf(zIndex)

    override fun Modifier.createModifier(): Modifier = zIndex(zIndex = zIndex)

    @Composable
    override fun Builder() = DefaultModifierFieldValueBuilder(
        modifierTextCode = buildAnnotatedString {
            appendLine(".zIndex(")

            append("  zIndex = ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("$zIndex")
            }
            append("f,")
            appendLine()

            append(")")
        },
        menuContent = {
            DefaultMenu {
                TextFieldItem(
                    label = "zIndex",
                    value = zIndex,
                    onValueChange = { zIndex = it },
                    transformer = FloatTransformer,
                )
            }
        },
    )

    class Factory(initialZIndex: Float? = null) : ModifierFieldValueFactory<ZIndexModifierFieldValue> {
        override val title: String = ".zIndex(...)"
        var zIndex by mutableStateOf(initialZIndex)

        override val canCreate: Boolean
            get() = zIndex != null

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)) = Column {
            TransformableTextField(
                value = zIndex,
                onValueChange = { zIndex = it },
                transformer = NullableFloatTransformer,
                textStyle = PreviewLabTheme.typography.label1,
                prefix = { Text("zIndex: ", style = PreviewLabTheme.typography.label2) },
            )

            Row { createButton() }
        }

        override fun create(): Result<ZIndexModifierFieldValue> = runCatching {
            ZIndexModifierFieldValue(
                zIndex = requireNotNull(zIndex) { "zIndex is null" },
            )
        }
    }
}

fun ModifierFieldValueList.zIndex(zIndex: Float) = then(
    ZIndexModifierFieldValue(
        zIndex = zIndex,
    ),
)
