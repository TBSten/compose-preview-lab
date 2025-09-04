package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.field.modifier.ModifierBuilder
import me.tbsten.compose.preview.lab.field.modifier.ModifierBuilderState
import me.tbsten.compose.preview.lab.field.modifier.ModifierFieldValue
import me.tbsten.compose.preview.lab.field.modifier.ModifierFieldValueList
import me.tbsten.compose.preview.lab.field.modifier.background
import me.tbsten.compose.preview.lab.field.modifier.border
import me.tbsten.compose.preview.lab.field.modifier.padding

class ModifierField(label: String, initialValue: ModifierFieldValueList = ModifierFieldValue.mark()) :
    ImmutablePreviewLabField<Modifier>(
        label = label,
        initialValue = Modifier,
    ) {
    private val builderState = ModifierBuilderState(initialValue)

    override var value: Modifier
        get() = builderState.values.createModifier()
        set(_) {
            error("ModifierField.setValue not supported.")
        }

    @Composable
    override fun Content() {
        ModifierBuilder(
            state = builderState,
        )
    }
}

fun ModifierFieldValueList.mark(color: Color = Color.Red.copy(alpha = 0.5f), borderWidth: Dp = 2.dp) = mark(
    borderColor = color,
    backgroundColor = color.copy(alpha = color.alpha * 0.5f),
    borderWidth = borderWidth,
)

fun ModifierFieldValueList.mark(
    borderColor: Color = Color.Red.copy(alpha = 0.75f),
    backgroundColor: Color = borderColor.copy(alpha = borderColor.alpha * 0.5f),
    borderWidth: Dp = 2.dp,
) = border(color = borderColor, width = borderWidth)
    .background(color = backgroundColor)
    .padding(all = borderWidth)
