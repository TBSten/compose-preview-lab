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
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.field.withTextHint
import me.tbsten.compose.preview.lab.sample.OnValueChange
import me.tbsten.compose.preview.lab.sample.SpeechBubbleBox
import me.tbsten.compose.preview.lab.sample.speechBubble
import org.jetbrains.compose.ui.tooling.preview.Preview

internal enum class StringFieldExampleSteps {
    EditValue,
    SeeResult,
}

@Preview
@ComposePreviewLabOption(id = "StringFieldExample")
@Composable
internal fun StringFieldExample() = PreviewLab {
    var step by remember { mutableStateOf(StringFieldExampleSteps.EditValue) }

    val text = fieldState {
        StringField("text", "Click Me")
            .speechBubble(
                bubbleText = "1. Edit the button text",
                alignment = Alignment.BottomStart,
                visible = { step == StringFieldExampleSteps.EditValue },
            )
    }.also { state ->
        OnValueChange(state) {
            step = StringFieldExampleSteps.SeeResult
        }
    }

    SpeechBubbleBox(
        bubbleText = "2. Button text updated!",
        visible = step == StringFieldExampleSteps.SeeResult,
        alignment = Alignment.BottomCenter,
    ) {
        StringFieldMyButton(text = text.value)
    }
}

internal enum class StringFieldWithPrefixSuffixExampleSteps {
    EditValue,
    SeeResult,
}

@Preview
@ComposePreviewLabOption(id = "StringFieldWithPrefixSuffixExample")
@Composable
internal fun StringFieldWithPrefixSuffixExample() = PreviewLab {
    var step by remember { mutableStateOf(StringFieldWithPrefixSuffixExampleSteps.EditValue) }

    val text = fieldState {
        StringField(
            label = "text",
            initialValue = "Click Me",
            prefix = { Text("$") },
            suffix = { Text("USD") },
        ).speechBubble(
            bubbleText = "1. Edit text with prefix/suffix",
            alignment = Alignment.BottomStart,
            visible = { step == StringFieldWithPrefixSuffixExampleSteps.EditValue },
        )
    }.also { state ->
        OnValueChange(state) {
            step = StringFieldWithPrefixSuffixExampleSteps.SeeResult
        }
    }

    SpeechBubbleBox(
        bubbleText = "2. Text updated!",
        visible = step == StringFieldWithPrefixSuffixExampleSteps.SeeResult,
        alignment = Alignment.BottomCenter,
    ) {
        StringFieldMyButton(text = text.value)
    }
}

internal enum class StringFieldWithTextHintExampleSteps {
    EditValue,
    SeeResult,
}

@Preview
@ComposePreviewLabOption(id = "StringFieldWithTextHintExample")
@Composable
internal fun StringFieldWithTextHintExample() = PreviewLab {
    var step by remember { mutableStateOf(StringFieldWithTextHintExampleSteps.EditValue) }

    val description = fieldState {
        StringField("description", "Short")
            .withTextHint()
            .speechBubble(
                bubbleText = "1. Select a text hint",
                alignment = Alignment.BottomStart,
                visible = { step == StringFieldWithTextHintExampleSteps.EditValue },
            )
    }.also { state ->
        OnValueChange(state) {
            step = StringFieldWithTextHintExampleSteps.SeeResult
        }
    }

    SpeechBubbleBox(
        bubbleText = "2. Description updated!",
        visible = step == StringFieldWithTextHintExampleSteps.SeeResult,
        alignment = Alignment.BottomCenter,
    ) {
        Text(description.value)
    }
}

@Composable
internal fun StringFieldMyButton(text: String) {
    Button(onClick = {}) {
        Text(text)
    }
}
