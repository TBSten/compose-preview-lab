package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.previewlab.PreviewLab
import me.tbsten.compose.preview.lab.field.EnumField
import me.tbsten.compose.preview.lab.sample.OnValueChange
import me.tbsten.compose.preview.lab.sample.SpeechBubbleBox
import me.tbsten.compose.preview.lab.sample.speechBubble
import androidx.compose.ui.tooling.preview.Preview

enum class ButtonVariant { Primary, Secondary, Tertiary }

internal enum class EnumFieldExampleSteps {
    SelectVariant,
    SeeResult,
}

@Preview
@ComposePreviewLabOption(id = "EnumFieldExample")
@Composable
internal fun EnumFieldExample() = PreviewLab {
    var step by remember { mutableStateOf(EnumFieldExampleSteps.SelectVariant) }

    val variant = fieldState {
        EnumField("Variant", ButtonVariant.Primary)
            .speechBubble(
                bubbleText = "1. Select a variant",
                alignment = Alignment.BottomStart,
                visible = { step == EnumFieldExampleSteps.SelectVariant },
            )
    }.also { state ->
        OnValueChange(state) {
            step = EnumFieldExampleSteps.SeeResult
        }
    }

    SpeechBubbleBox(
        bubbleText = "2. Button style changed!",
        visible = step == EnumFieldExampleSteps.SeeResult,
        alignment = Alignment.BottomCenter,
    ) {
        EnumFieldMyButton(variant = variant.value)
    }
}

@Composable
internal fun EnumFieldMyButton(variant: ButtonVariant) {
    val colors = when (variant) {
        ButtonVariant.Primary -> ButtonDefaults.buttonColors()
        ButtonVariant.Secondary -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
        )
        ButtonVariant.Tertiary -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
        )
    }
    Button(onClick = {}, colors = colors) {
        Text(variant.name)
    }
}
