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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.previewlab.PreviewLab
import me.tbsten.compose.preview.lab.field.FloatField
import me.tbsten.compose.preview.lab.sample.OnValueChange
import me.tbsten.compose.preview.lab.sample.SpeechBubbleBox
import me.tbsten.compose.preview.lab.sample.speechBubble
import org.jetbrains.compose.ui.tooling.preview.Preview

internal enum class FloatFieldExampleSteps {
    EditValue,
    SeeResult,
}

@Preview
@ComposePreviewLabOption(id = "FloatFieldExample")
@Composable
internal fun FloatFieldExample() = PreviewLab {
    var step by remember { mutableStateOf(FloatFieldExampleSteps.EditValue) }

    val alpha = fieldState {
        FloatField("Alpha", 0.5f)
            .speechBubble(
                bubbleText = "1. Adjust the alpha",
                alignment = Alignment.BottomStart,
                visible = { step == FloatFieldExampleSteps.EditValue },
            )
    }.also { state ->
        OnValueChange(state) {
            step = FloatFieldExampleSteps.SeeResult
        }
    }

    SpeechBubbleBox(
        bubbleText = "2. Transparency changed!",
        visible = step == FloatFieldExampleSteps.SeeResult,
        alignment = Alignment.BottomCenter,
    ) {
        TransparentBox(alpha = alpha.value)
    }
}

@Composable
internal fun TransparentBox(alpha: Float) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.Blue.copy(alpha = alpha)),
    )
}
