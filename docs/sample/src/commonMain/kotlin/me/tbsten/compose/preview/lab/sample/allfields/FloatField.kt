package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.sample.component.SamplePreviewLab
import me.tbsten.compose.preview.lab.field.FloatField
import me.tbsten.compose.preview.lab.sample.OnValueChange
import me.tbsten.compose.preview.lab.sample.SpeechBubbleBox
import me.tbsten.compose.preview.lab.sample.speechBubble
import androidx.compose.ui.tooling.preview.Preview

internal enum class FloatFieldExampleSteps {
    EditValue,
    SeeResult,
}

/**
 * Demonstrates [FloatField] for editing floating-point values.
 *
 * Adjust the alpha value (0.0-1.0) to control the box's transparency.
 * Shows both raw and clamped values with a visual progress bar.
 */
@Preview
@ComposePreviewLabOption(id = "FloatFieldExample")
@Composable
internal fun FloatFieldExample() = SamplePreviewLab {
    var step by remember { mutableStateOf(FloatFieldExampleSteps.EditValue) }

    val alpha = fieldState {
        FloatField("Alpha (0..1)", 0.6f)
            .speechBubble(
                bubbleText = "1. Adjust alpha (0..1 recommended)",
                alignment = Alignment.BottomStart,
                visible = { step == FloatFieldExampleSteps.EditValue },
            )
    }.also { state ->
        OnValueChange(state) {
            step = FloatFieldExampleSteps.SeeResult
        }
    }

    SpeechBubbleBox(
        bubbleText = "2. Transparency changed!",
        visible = step == FloatFieldExampleSteps.SeeResult,
        alignment = Alignment.BottomCenter,
    ) {
        AlphaPreview(alpha = alpha.value)
    }
}

@Composable
internal fun AlphaPreview(alpha: Float) {
    val clamped = alpha.coerceIn(0f, 1f)
    val bar = buildString {
        val filled = (clamped * 20).toInt()
        append("[")
        append("#".repeat(filled))
        append("-".repeat(20 - filled))
        append("]")
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(4.dp),
    ) {
        Text("Alpha Preview", style = MaterialTheme.typography.titleMedium)
        Text("raw: $alpha  clamped: $clamped", style = MaterialTheme.typography.bodySmall)
        Text(bar, style = MaterialTheme.typography.bodySmall)
        Box(
            modifier = Modifier
                .size(140.dp)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.Blue.copy(alpha = clamped)),
            )
            Text(
                "OVERLAY",
                color = Color.Black,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
