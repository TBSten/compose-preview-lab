package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.DpSizeField
import me.tbsten.compose.preview.lab.sample.OnValueChange
import me.tbsten.compose.preview.lab.sample.SpeechBubbleBox
import me.tbsten.compose.preview.lab.sample.speechBubble
import org.jetbrains.compose.ui.tooling.preview.Preview

internal enum class DpSizeFieldExampleSteps {
    EditValue,
    SeeResult,
}

@Preview
@ComposePreviewLabOption(id = "DpSizeFieldExample")
@Composable
internal fun DpSizeFieldExample() = PreviewLab {
    var step by remember { mutableStateOf(DpSizeFieldExampleSteps.EditValue) }

    val buttonSize = fieldState {
        DpSizeField("Button Size", DpSize(120.dp, 48.dp))
            .speechBubble(
                bubbleText = "1. Resize the button",
                alignment = Alignment.BottomStart,
                visible = { step == DpSizeFieldExampleSteps.EditValue },
            )
    }.also { state ->
        OnValueChange(state) {
            step = DpSizeFieldExampleSteps.SeeResult
        }
    }

    SpeechBubbleBox(
        bubbleText = "2. Button size changed!",
        visible = step == DpSizeFieldExampleSteps.SeeResult,
        alignment = Alignment.BottomCenter,
    ) {
        SizedButton(size = buttonSize.value)
    }
}

@Composable
internal fun SizedButton(size: DpSize) {
    Button(
        onClick = { },
        modifier = Modifier.size(size),
    ) {
        Text("Sized Button")
    }
}
