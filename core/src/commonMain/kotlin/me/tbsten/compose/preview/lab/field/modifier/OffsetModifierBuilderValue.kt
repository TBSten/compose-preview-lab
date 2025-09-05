package me.tbsten.compose.preview.lab.field.modifier

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
        override fun Content(createButton: @Composable (() -> Unit)) = Column {
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

fun ModifierFieldValueList.offset(x: Dp = 0.dp, y: Dp = 0.dp) = then(
    OffsetModifierFieldValue(
        x = x,
        y = y,
    ),
)

fun ModifierFieldValueList.offset(offset: Dp = 0.dp) = then(
    OffsetModifierFieldValue(
        x = offset,
        y = offset,
    ),
)
