package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.LongField
import me.tbsten.compose.preview.lab.sample.OnValueChange
import me.tbsten.compose.preview.lab.sample.SpeechBubbleBox
import me.tbsten.compose.preview.lab.sample.speechBubble
import org.jetbrains.compose.ui.tooling.preview.Preview

internal enum class LongFieldExampleSteps {
    EditValue,
    SeeResult,
}

@Preview
@ComposePreviewLabOption(id = "LongFieldExample")
@Composable
internal fun LongFieldExample() = PreviewLab {
    var step by remember { mutableStateOf(LongFieldExampleSteps.EditValue) }

    val timestamp = fieldState {
        LongField("Timestamp", 0L)
            .speechBubble(
                bubbleText = "1. Enter a timestamp",
                alignment = Alignment.BottomStart,
                visible = { step == LongFieldExampleSteps.EditValue },
            )
    }.also { state ->
        OnValueChange(state) {
            step = LongFieldExampleSteps.SeeResult
        }
    }

    SpeechBubbleBox(
        bubbleText = "2. Timestamp updated!",
        visible = step == LongFieldExampleSteps.SeeResult,
        alignment = Alignment.BottomCenter,
    ) {
        DateDisplay(timestamp = timestamp.value)
    }
}

@Composable
internal fun DateDisplay(timestamp: Long) {
    Text("Timestamp: $timestamp")
}
