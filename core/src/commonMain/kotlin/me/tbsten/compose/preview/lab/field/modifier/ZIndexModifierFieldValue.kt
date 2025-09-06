package me.tbsten.compose.preview.lab.field.modifier

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import me.tbsten.compose.preview.lab.component.FloatTransformer
import me.tbsten.compose.preview.lab.component.NullableFloatTransformer
import me.tbsten.compose.preview.lab.component.TransformableTextField
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Text

/**
 * ModifierFieldValue that controls the layering order (Z-index) of a composable.
 *
 * @param zIndex The Z-index value determining the stacking order. Higher values appear on top of lower values.
 */
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

    /**
     * Factory for creating ZIndexModifierFieldValue instances with configurable initial values.
     *
     * @param initialZIndex Initial Z-index value for controlling the layering order
     */
    class Factory(initialZIndex: Float? = null) : ModifierFieldValueFactory<ZIndexModifierFieldValue> {
        override val title: String = ".zIndex(...)"
        var zIndex by mutableStateOf(initialZIndex)

        override val canCreate: Boolean
            get() = zIndex != null

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)) = Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
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

/**
 * Sets the Z-index (layering order) for this modifier list.
 *
 * @param zIndex The Z-index value determining the stacking order (higher values appear on top)
 * @return A new ModifierFieldValueList with Z-index applied
 */
fun ModifierFieldValueList.zIndex(zIndex: Float) = then(
    ZIndexModifierFieldValue(
        zIndex = zIndex,
    ),
)
