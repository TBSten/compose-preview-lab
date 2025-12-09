package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
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
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.ColorField
import me.tbsten.compose.preview.lab.field.withPredefinedColorHint
import me.tbsten.compose.preview.lab.sample.OnValueChange
import me.tbsten.compose.preview.lab.sample.SpeechBubbleBox
import me.tbsten.compose.preview.lab.sample.speechBubble
import org.jetbrains.compose.ui.tooling.preview.Preview

internal enum class ColorFieldExampleSteps {
    SelectColor,
    SeeResult,
}

@Preview
@ComposePreviewLabOption(id = "ColorFieldExample")
@Composable
internal fun ColorFieldExample() = PreviewLab {
    var step by remember { mutableStateOf(ColorFieldExampleSteps.SelectColor) }

    val color = fieldState {
        ColorField("Background", Color.Blue)
            .withPredefinedColorHint()
            .speechBubble(
                bubbleText = "1. Pick a color",
                alignment = Alignment.BottomStart,
                visible = { step == ColorFieldExampleSteps.SelectColor },
            )
    }.also { state ->
        OnValueChange(state) {
            step = ColorFieldExampleSteps.SeeResult
        }
    }

    SpeechBubbleBox(
        bubbleText = "2. Color changed!",
        visible = step == ColorFieldExampleSteps.SeeResult,
        alignment = Alignment.BottomCenter,
    ) {
        ColoredBox(color = color.value)
    }
}

@Composable
internal fun ColoredBox(color: Color) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(color),
    )
}
