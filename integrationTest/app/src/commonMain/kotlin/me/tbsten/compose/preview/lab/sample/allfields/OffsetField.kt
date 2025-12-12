package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.previewlab.PreviewLab
import me.tbsten.compose.preview.lab.field.OffsetField
import me.tbsten.compose.preview.lab.sample.OnValueChange
import me.tbsten.compose.preview.lab.sample.SpeechBubbleBox
import me.tbsten.compose.preview.lab.sample.speechBubble
import org.jetbrains.compose.ui.tooling.preview.Preview

internal enum class OffsetFieldExampleSteps {
    EditValue,
    SeeResult,
}

@Preview
@ComposePreviewLabOption(id = "OffsetFieldExample")
@Composable
internal fun OffsetFieldExample() = PreviewLab {
    var step by remember { mutableStateOf(OffsetFieldExampleSteps.EditValue) }

    val offset = fieldState {
        OffsetField("Position", Offset(50f, 100f))
            .speechBubble(
                bubbleText = "1. Change the position",
                alignment = Alignment.BottomStart,
                visible = { step == OffsetFieldExampleSteps.EditValue },
            )
    }.also { state ->
        OnValueChange(state) {
            step = OffsetFieldExampleSteps.SeeResult
        }
    }

    SpeechBubbleBox(
        bubbleText = "2. Box position updated!",
        visible = step == OffsetFieldExampleSteps.SeeResult,
        alignment = Alignment.BottomCenter,
    ) {
        TranslatedBox(offset = offset.value)
    }
}

@Composable
internal fun TranslatedBox(offset: Offset) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .graphicsLayer {
                translationX = offset.x
                translationY = offset.y
            }
            .background(Color.Blue),
    )
}
