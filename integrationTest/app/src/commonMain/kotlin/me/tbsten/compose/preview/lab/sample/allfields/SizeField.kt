package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.SizeField
import me.tbsten.compose.preview.lab.sample.OnValueChange
import me.tbsten.compose.preview.lab.sample.SpeechBubbleBox
import me.tbsten.compose.preview.lab.sample.speechBubble
import org.jetbrains.compose.ui.tooling.preview.Preview

internal enum class SizeFieldExampleSteps {
    EditValue,
    SeeResult,
}

@Preview
@ComposePreviewLabOption(id = "SizeFieldExample")
@Composable
internal fun SizeFieldExample() = PreviewLab {
    var step by remember { mutableStateOf(SizeFieldExampleSteps.EditValue) }

    val canvasSize = fieldState {
        SizeField("Canvas", Size(200f, 150f))
            .speechBubble(
                bubbleText = "1. Change the size",
                alignment = Alignment.BottomStart,
                visible = { step == SizeFieldExampleSteps.EditValue },
            )
    }.also { state ->
        OnValueChange(state) {
            step = SizeFieldExampleSteps.SeeResult
        }
    }

    SpeechBubbleBox(
        bubbleText = "2. Canvas size updated!",
        visible = step == SizeFieldExampleSteps.SeeResult,
        alignment = Alignment.BottomCenter,
    ) {
        SizedCanvas(canvasSize = canvasSize.value)
    }
}

@Composable
internal fun SizedCanvas(canvasSize: Size) {
    Canvas(modifier = Modifier.size(200.dp)) {
        drawRect(Color.Blue, size = canvasSize)
    }
}
