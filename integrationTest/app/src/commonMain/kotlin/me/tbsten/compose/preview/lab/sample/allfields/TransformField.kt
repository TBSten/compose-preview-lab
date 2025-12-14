package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.foundation.layout.Column
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
import me.tbsten.compose.preview.lab.field.transform
import me.tbsten.compose.preview.lab.sample.OnValueChange
import me.tbsten.compose.preview.lab.sample.SpeechBubbleBox
import me.tbsten.compose.preview.lab.sample.speechBubble
import org.jetbrains.compose.ui.tooling.preview.Preview

internal enum class TransformFieldExampleSteps {
    EditValue,
    SeeResult,
}

@Preview
@ComposePreviewLabOption(id = "TransformFieldExample")
@Composable
internal fun TransformFieldExample() = PreviewLab {
    var step by remember { mutableStateOf(TransformFieldExampleSteps.EditValue) }

    val intValue = fieldState {
        StringField("number", "42")
            .transform(
                transform = { it.toIntOrNull() ?: 0 },
                reverse = { it.toString() },
            )
            .speechBubble(
                bubbleText = "1. Enter a number string",
                alignment = Alignment.BottomStart,
                visible = { step == TransformFieldExampleSteps.EditValue },
            )
    }.also { state ->
        OnValueChange(state) {
            step = TransformFieldExampleSteps.SeeResult
        }
    }

    SpeechBubbleBox(
        bubbleText = "2. String transformed to Int!",
        visible = step == TransformFieldExampleSteps.SeeResult,
        alignment = Alignment.BottomCenter,
    ) {
        TransformedValue(intValue = intValue.value)
    }
}

@Composable
internal fun TransformedValue(intValue: Int) {
    Column {
        Text("intValue: $intValue")
        Text("intValue::class: ${intValue::class.simpleName}")
    }
}
