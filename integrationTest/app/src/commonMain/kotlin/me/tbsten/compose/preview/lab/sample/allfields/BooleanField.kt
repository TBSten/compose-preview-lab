package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.BooleanField
import me.tbsten.compose.preview.lab.sample.OnValueChange
import me.tbsten.compose.preview.lab.sample.SpeechBubbleBox
import me.tbsten.compose.preview.lab.sample.speechBubble
import org.jetbrains.compose.ui.tooling.preview.Preview

internal enum class BooleanFieldExampleSteps {
    ToggleSwitch,
    SeeButtonState,
}

@Preview
@ComposePreviewLabOption(id = "BooleanFieldExample")
@Composable
internal fun BooleanFieldExample() = PreviewLab {
    var step by remember { mutableStateOf(BooleanFieldExampleSteps.ToggleSwitch) }

    val enabled = fieldState {
        BooleanField("enabled", true)
            .speechBubble(
                bubbleText = "1. Please toggle switch",
                alignment = Alignment.BottomStart,
                visible = { step == BooleanFieldExampleSteps.ToggleSwitch },
            )
    }.also { enabled ->
        OnValueChange(enabled) {
            step = BooleanFieldExampleSteps.SeeButtonState
        }
    }

    SpeechBubbleBox(
        bubbleText = "2. You can change button ui!",
        visible = step == BooleanFieldExampleSteps.SeeButtonState,
        alignment = Alignment.BottomCenter,
    ) {
        BooleanFieldMyButton(enabled = enabled.value)
    }
}

@Composable
internal fun BooleanFieldMyButton(enabled: Boolean) {
    Button(onClick = {}, enabled = enabled) {
        Text("Button")
    }
}
