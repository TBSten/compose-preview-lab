package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.previewlab.PreviewLab
import me.tbsten.compose.preview.lab.field.ByteField
import me.tbsten.compose.preview.lab.sample.OnValueChange
import me.tbsten.compose.preview.lab.sample.SpeechBubbleBox
import me.tbsten.compose.preview.lab.sample.speechBubble
import androidx.compose.ui.tooling.preview.Preview

internal enum class ByteFieldExampleSteps {
    EditValue,
    SeeResult,
}

/**
 * Demonstrates [ByteField] for editing byte values (-128 to 127).
 *
 * Enter a byte value to update the flag display.
 * Useful for low-level data manipulation and flag testing.
 */
@Preview
@ComposePreviewLabOption(id = "ByteFieldExample")
@Composable
internal fun ByteFieldExample() = PreviewLab {
    var step by remember { mutableStateOf(ByteFieldExampleSteps.EditValue) }

    val flag = fieldState {
        ByteField("Flag", 0)
            .speechBubble(
                bubbleText = "1. Enter a byte value",
                alignment = Alignment.BottomStart,
                visible = { step == ByteFieldExampleSteps.EditValue },
            )
    }.also { state ->
        OnValueChange(state) {
            step = ByteFieldExampleSteps.SeeResult
        }
    }

    SpeechBubbleBox(
        bubbleText = "2. Flag value updated!",
        visible = step == ByteFieldExampleSteps.SeeResult,
        alignment = Alignment.BottomCenter,
    ) {
        FlagDisplay(flag = flag.value)
    }
}

@Composable
internal fun FlagDisplay(flag: Byte) {
    Text("Flag: $flag")
}
