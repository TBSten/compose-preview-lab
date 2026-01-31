package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import me.tbsten.compose.preview.lab.sample.component.SamplePreviewLab
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.field.transform
import me.tbsten.compose.preview.lab.sample.OnValueChange
import me.tbsten.compose.preview.lab.sample.SpeechBubbleBox
import me.tbsten.compose.preview.lab.sample.speechBubble
import androidx.compose.ui.tooling.preview.Preview

internal enum class TransformFieldExampleSteps {
    EditValue,
    SeeResult,
}

/**
 * Demonstrates [transform] extension for converting between field and target types.
 *
 * Enter a number as text; it's parsed to Int (or null if invalid).
 * Useful when you need string-based UI but typed values in code.
 */
@Preview
@ComposePreviewLabOption(id = "TransformFieldExample")
@Composable
internal fun TransformFieldExample() = SamplePreviewLab {
    var step by remember { mutableStateOf(TransformFieldExampleSteps.EditValue) }

    val intValue = fieldState {
        StringField("number", "42")
            .transform(
                transform = { it.toIntOrNull() },
                reverse = { it?.toString() ?: "" },
            )
            .speechBubble(
                bubbleText = "1. Enter an integer (invalid => null)",
                alignment = Alignment.BottomStart,
                visible = { step == TransformFieldExampleSteps.EditValue },
            )
    }.also { state ->
        OnValueChange(state) {
            step = TransformFieldExampleSteps.SeeResult
        }
    }

    SpeechBubbleBox(
        bubbleText = "2. Parsed value updated!",
        visible = step == TransformFieldExampleSteps.SeeResult,
        alignment = Alignment.BottomCenter,
    ) {
        TransformedValue(intValue = intValue.value)
    }
}

@Composable
internal fun TransformedValue(intValue: Int?) {
    Column(modifier = Modifier.padding(4.dp)) {
        Text("Parse result", style = MaterialTheme.typography.titleMedium)
        if (intValue == null) {
            Text(
                "Invalid input (null)",
                color = Color(0xFFD32F2F),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Use TransformField when you want \"string UI\" but typed value in code.",
                style = MaterialTheme.typography.bodySmall,
            )
        } else {
            Text("intValue: $intValue", style = MaterialTheme.typography.bodyMedium)
            Text("double: ${intValue * 2}", style = MaterialTheme.typography.bodyMedium)
            Text(
                "progress: [" + "#".repeat((intValue.coerceIn(0, 10))) + "-".repeat(10 - intValue.coerceIn(0, 10)) + "]",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
