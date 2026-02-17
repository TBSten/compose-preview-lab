package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.sample.component.SamplePreviewLab
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.field.nullable
import me.tbsten.compose.preview.lab.sample.OnValueChange
import me.tbsten.compose.preview.lab.sample.SpeechBubbleBox
import me.tbsten.compose.preview.lab.sample.speechBubble
import androidx.compose.ui.tooling.preview.Preview

internal enum class NullableFieldExampleSteps {
    EditValue,
    SeeResult,
}

/**
 * Demonstrates the [nullable] extension for making any field nullable.
 *
 * Toggle the null checkbox to switch between null and a [StringField] value.
 * Tests how your UI handles optional/nullable data.
 */
@Preview
@ComposePreviewLabOption(id = "NullableFieldExample")
@Composable
internal fun NullableFieldExample() = SamplePreviewLab {
    var step by remember { mutableStateOf(NullableFieldExampleSteps.EditValue) }

    val userName = fieldState {
        StringField("User Name", "John Doe")
            .nullable()
            .speechBubble(
                bubbleText = "1. Toggle null or edit text",
                alignment = Alignment.BottomStart,
                visible = { step == NullableFieldExampleSteps.EditValue },
            )
    }.also { state ->
        OnValueChange(state) {
            step = NullableFieldExampleSteps.SeeResult
        }
    }

    SpeechBubbleBox(
        bubbleText = "2. Value updated!",
        visible = step == NullableFieldExampleSteps.SeeResult,
        alignment = Alignment.BottomCenter,
    ) {
        UserName(userName = userName.value)
    }
}

@Composable
internal fun UserName(userName: String?) {
    Text(userName ?: "No user name")
}
