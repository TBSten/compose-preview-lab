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
import me.tbsten.compose.preview.lab.previewlab.PreviewLab
import me.tbsten.compose.preview.lab.field.BooleanField
import me.tbsten.compose.preview.lab.sample.OnValueChange
import me.tbsten.compose.preview.lab.sample.SpeechBubbleBox
import me.tbsten.compose.preview.lab.sample.speechBubble
import androidx.compose.ui.tooling.preview.Preview
import me.tbsten.compose.preview.lab.event.withEvent
import me.tbsten.compose.preview.lab.sample.component.SamplePreviewLab

internal enum class BooleanFieldExampleSteps {
    ToggleSwitch,
    SeeButtonState,
}

/**
 * Demonstrates [BooleanField] for toggling a boolean value.
 *
 * Toggle the switch to enable/disable the button.
 * The button's `enabled` state is controlled by the field value.
 */
@Preview
@ComposePreviewLabOption(id = "BooleanFieldExample")
@Composable
internal fun BooleanFieldExample() = SamplePreviewLab {
    var step by remember { mutableStateOf(BooleanFieldExampleSteps.ToggleSwitch) }

    val enabled = fieldState {
        BooleanField("enabled", true)
//            .speechBubble(
//                bubbleText = "1. Toggle the switch",
//                alignment = Alignment.BottomStart,
//                visible = { step == BooleanFieldExampleSteps.ToggleSwitch },
//            )
    }.also { enabled ->
        OnValueChange(enabled) {
            step = BooleanFieldExampleSteps.SeeButtonState
        }
    }

//    SpeechBubbleBox(
//        bubbleText = "2. Button UI changed!",
//        visible = step == BooleanFieldExampleSteps.SeeButtonState,
//        alignment = Alignment.BottomCenter,
//    ) {
        BooleanFieldMyButton(enabled = enabled.value, onClick = withEvent("onClick"))
//    }
}

@Composable
internal fun BooleanFieldMyButton(enabled: Boolean, onClick: ()->Unit) {
    Button(onClick = onClick, enabled = enabled) {
        Text("Button")
    }
}
