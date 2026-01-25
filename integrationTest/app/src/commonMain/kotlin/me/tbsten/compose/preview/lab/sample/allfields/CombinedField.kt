package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.previewlab.PreviewLab
import me.tbsten.compose.preview.lab.field.CombinedField
import me.tbsten.compose.preview.lab.field.DpField
import me.tbsten.compose.preview.lab.field.combined
import me.tbsten.compose.preview.lab.field.splitedOf
import me.tbsten.compose.preview.lab.sample.OnValueChange
import me.tbsten.compose.preview.lab.sample.SpeechBubbleBox
import me.tbsten.compose.preview.lab.sample.speechBubble
import androidx.compose.ui.tooling.preview.Preview

internal data class CombinedFieldPadding(val horizontal: Dp, val vertical: Dp)

internal enum class CombinedFieldExampleSteps {
    EditValue,
    SeeResult,
}

/**
 * Demonstrates [CombinedField] for grouping multiple fields into a single value.
 *
 * Combines horizontal and vertical [DpField]s into a [CombinedFieldPadding] data class.
 * Useful when you need to edit related values as a cohesive unit.
 */
@Preview
@ComposePreviewLabOption(id = "CombinedFieldExample")
@Composable
internal fun CombinedFieldExample() = PreviewLab {
    var step by remember { mutableStateOf(CombinedFieldExampleSteps.EditValue) }

    val padding = fieldState {
        CombinedField(
            label = "Padding",
            fields = listOf(
                DpField("Horizontal", 16.dp),
                DpField("Vertical", 8.dp),
            ),
            combine = { values ->
                @Suppress("UNCHECKED_CAST")
                CombinedFieldPadding(values[0] as Dp, values[1] as Dp)
            },
            split = { listOf(it.horizontal, it.vertical) },
        ).speechBubble(
            bubbleText = "1. Adjust padding values",
            alignment = Alignment.BottomStart,
            visible = { step == CombinedFieldExampleSteps.EditValue },
        )
    }.also { state ->
        OnValueChange(state) {
            step = CombinedFieldExampleSteps.SeeResult
        }
    }

    SpeechBubbleBox(
        bubbleText = "2. Padding updated!",
        visible = step == CombinedFieldExampleSteps.SeeResult,
        alignment = Alignment.BottomCenter,
    ) {
        PaddedBox(padding = padding.value)
    }
}

@Composable
internal fun PaddedBox(padding: CombinedFieldPadding) {
    Box(
        modifier = Modifier
            .background(Color.LightGray)
            .padding(horizontal = padding.horizontal, vertical = padding.vertical),
    ) {
        Text("Content")
    }
}
