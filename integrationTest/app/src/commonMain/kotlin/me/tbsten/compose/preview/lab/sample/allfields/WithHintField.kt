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
import me.tbsten.compose.preview.lab.previewlab.PreviewLab
import me.tbsten.compose.preview.lab.field.SpField
import me.tbsten.compose.preview.lab.field.withHint
import me.tbsten.compose.preview.lab.sample.OnValueChange
import me.tbsten.compose.preview.lab.sample.SpeechBubbleBox
import me.tbsten.compose.preview.lab.sample.speechBubble
import org.jetbrains.compose.ui.tooling.preview.Preview

internal enum class WithHintFieldExampleSteps {
    SelectHint,
    SeeResult,
}

@Preview
@ComposePreviewLabOption(id = "WithHintFieldExample")
@Composable
internal fun WithHintFieldExample() = PreviewLab {
    var step by remember { mutableStateOf(WithHintFieldExampleSteps.SelectHint) }

    val fontSize = fieldState {
        SpField(label = "Font Size", initialValue = 16.sp)
            .withHint(
                "Small" to 12.sp,
                "Medium" to 16.sp,
                "Large" to 20.sp,
                "XLarge" to 24.sp,
            )
            .speechBubble(
                bubbleText = "1. Select a hint or enter value",
                alignment = Alignment.BottomStart,
                visible = { step == WithHintFieldExampleSteps.SelectHint },
            )
    }.also { state ->
        OnValueChange(state) {
            step = WithHintFieldExampleSteps.SeeResult
        }
    }

    SpeechBubbleBox(
        bubbleText = "2. Font size updated!",
        visible = step == WithHintFieldExampleSteps.SeeResult,
        alignment = Alignment.BottomCenter,
    ) {
        HintedText(fontSize = fontSize.value)
    }
}

@Composable
internal fun HintedText(fontSize: TextUnit) {
    Text(
        text = "Sample Text",
        fontSize = fontSize,
    )
}
