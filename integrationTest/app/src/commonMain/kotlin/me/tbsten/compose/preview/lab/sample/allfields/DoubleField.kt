package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.previewlab.PreviewLab
import me.tbsten.compose.preview.lab.field.DoubleField
import me.tbsten.compose.preview.lab.sample.OnValueChange
import me.tbsten.compose.preview.lab.sample.SpeechBubbleBox
import me.tbsten.compose.preview.lab.sample.speechBubble
import androidx.compose.ui.tooling.preview.Preview

internal enum class DoubleFieldExampleSteps {
    EditValue,
    SeeResult,
}

@Preview
@ComposePreviewLabOption(id = "DoubleFieldExample")
@Composable
internal fun DoubleFieldExample() = PreviewLab {
    var step by remember { mutableStateOf(DoubleFieldExampleSteps.EditValue) }

    val price = fieldState {
        DoubleField("Price", 99.99)
            .speechBubble(
                bubbleText = "1. Change the price",
                alignment = Alignment.BottomStart,
                visible = { step == DoubleFieldExampleSteps.EditValue },
            )
    }.also { state ->
        OnValueChange(state) {
            step = DoubleFieldExampleSteps.SeeResult
        }
    }

    SpeechBubbleBox(
        bubbleText = "2. Price tag updated!",
        visible = step == DoubleFieldExampleSteps.SeeResult,
        alignment = Alignment.BottomCenter,
    ) {
        PriceTag(price = price.value)
    }
}

@Composable
internal fun PriceTag(price: Double) {
    Text("Price: $$price")
}
