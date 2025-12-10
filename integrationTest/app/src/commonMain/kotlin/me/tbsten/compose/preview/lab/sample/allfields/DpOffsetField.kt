package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.DpOffsetField
import me.tbsten.compose.preview.lab.sample.OnValueChange
import me.tbsten.compose.preview.lab.sample.SpeechBubbleBox
import me.tbsten.compose.preview.lab.sample.speechBubble
import org.jetbrains.compose.ui.tooling.preview.Preview

internal enum class DpOffsetFieldExampleSteps {
    EditValue,
    SeeResult,
}

@Preview
@ComposePreviewLabOption(id = "DpOffsetFieldExample")
@Composable
internal fun DpOffsetFieldExample() = PreviewLab {
    var step by remember { mutableStateOf(DpOffsetFieldExampleSteps.EditValue) }

    val offset = fieldState {
        DpOffsetField("Offset", DpOffset(16.dp, 8.dp))
            .speechBubble(
                bubbleText = "1. Change the offset",
                alignment = Alignment.BottomStart,
                visible = { step == DpOffsetFieldExampleSteps.EditValue },
            )
    }.also { state ->
        OnValueChange(state) {
            step = DpOffsetFieldExampleSteps.SeeResult
        }
    }

    SpeechBubbleBox(
        bubbleText = "2. Position updated!",
        visible = step == DpOffsetFieldExampleSteps.SeeResult,
        alignment = Alignment.BottomCenter,
    ) {
        PositionedText(offset = offset.value)
    }
}

@Composable
internal fun PositionedText(offset: DpOffset) {
    Text(
        text = "Positioned Text",
        modifier = Modifier.offset(offset.x, offset.y),
    )
}
