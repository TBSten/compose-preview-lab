package me.tbsten.compose.preview.lab.sample.allfields

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.previewlab.PreviewLab
import me.tbsten.compose.preview.lab.field.DpField
import me.tbsten.compose.preview.lab.sample.OnValueChange
import me.tbsten.compose.preview.lab.sample.SpeechBubbleBox
import me.tbsten.compose.preview.lab.sample.speechBubble
import androidx.compose.ui.tooling.preview.Preview

internal enum class DpFieldExampleSteps {
    EditValue,
    SeeResult,
}

@Preview
@ComposePreviewLabOption(id = "DpFieldExample")
@Composable
internal fun DpFieldExample() = PreviewLab {
    var step by remember { mutableStateOf(DpFieldExampleSteps.EditValue) }

    val padding = fieldState {
        DpField("Padding", 16.dp)
            .speechBubble(
                bubbleText = "1. Adjust the padding",
                alignment = Alignment.BottomStart,
                visible = { step == DpFieldExampleSteps.EditValue },
            )
    }.also { state ->
        OnValueChange(state) {
            step = DpFieldExampleSteps.SeeResult
        }
    }

    SpeechBubbleBox(
        bubbleText = "2. Padding changed!",
        visible = step == DpFieldExampleSteps.SeeResult,
        alignment = Alignment.BottomCenter,
    ) {
        PaddedContent(padding = padding.value)
    }
}

@Composable
internal fun PaddedContent(padding: Dp) {
    Box(modifier = Modifier.padding(padding)) {
        Text("Padded Content")
    }
}
