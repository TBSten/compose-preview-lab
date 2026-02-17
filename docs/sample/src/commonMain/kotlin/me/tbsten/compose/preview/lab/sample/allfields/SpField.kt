package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.sample.component.SamplePreviewLab
import me.tbsten.compose.preview.lab.field.SpField
import me.tbsten.compose.preview.lab.sample.OnValueChange
import me.tbsten.compose.preview.lab.sample.SpeechBubbleBox
import me.tbsten.compose.preview.lab.sample.speechBubble
import androidx.compose.ui.tooling.preview.Preview

internal enum class SpFieldExampleSteps {
    EditValue,
    SeeResult,
}

/**
 * Demonstrates [SpField] for editing text size in sp (scalable pixels).
 *
 * Adjust the font size to see text scaling in real-time.
 * Sp units respect user's font size preferences for accessibility.
 */
@Preview
@ComposePreviewLabOption(id = "SpFieldExample")
@Composable
internal fun SpFieldExample() = SamplePreviewLab {
    var step by remember { mutableStateOf(SpFieldExampleSteps.EditValue) }

    val fontSize = fieldState {
        SpField("Font Size", 16.sp)
            .speechBubble(
                bubbleText = "1. Change the font size",
                alignment = Alignment.BottomStart,
                visible = { step == SpFieldExampleSteps.EditValue },
            )
    }.also { state ->
        OnValueChange(state) {
            step = SpFieldExampleSteps.SeeResult
        }
    }

    SpeechBubbleBox(
        bubbleText = "2. Text size updated!",
        visible = step == SpFieldExampleSteps.SeeResult,
        alignment = Alignment.BottomCenter,
    ) {
        SizedText(fontSize = fontSize.value)
    }
}

@Composable
internal fun SizedText(fontSize: TextUnit) {
    Text(
        text = "Sample Text",
        fontSize = fontSize,
    )
}
