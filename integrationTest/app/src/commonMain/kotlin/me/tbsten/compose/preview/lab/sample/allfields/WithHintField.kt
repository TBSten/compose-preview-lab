package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.field.ListField
import me.tbsten.compose.preview.lab.field.SpField
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.field.withHint
import me.tbsten.compose.preview.lab.field.withHintAction
import me.tbsten.compose.preview.lab.previewlab.PreviewLab
import me.tbsten.compose.preview.lab.sample.OnValueChange
import me.tbsten.compose.preview.lab.sample.SpeechBubbleBox
import me.tbsten.compose.preview.lab.sample.speechBubble

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

internal enum class WithHintActionExampleSteps {
    SelectAction,
    SeeResult,
}

@Preview
@ComposePreviewLabOption(id = "WithHintActionExample")
@Composable
internal fun WithHintActionExample() = PreviewLab {
    var step by remember { mutableStateOf(WithHintActionExampleSteps.SelectAction) }

    val items = fieldState {
        ListField(
            label = "Items",
            elementField = { StringField(label = "Item", initialValue = "") },
            initialValue = emptyList<String>(),
        )
            .withHintAction(
                "Add 3 items" to {
                    value = value + listOf("Item A", "Item B", "Item C")
                },
                "Clear all" to {
                    value = emptyList()
                },
            )
            .speechBubble(
                bubbleText = "1. Click an action hint",
                alignment = Alignment.BottomStart,
                visible = { step == WithHintActionExampleSteps.SelectAction },
            )
    }.also { state ->
        OnValueChange(state) {
            step = WithHintActionExampleSteps.SeeResult
        }
    }

    SpeechBubbleBox(
        bubbleText = "2. Items updated!",
        visible = step == WithHintActionExampleSteps.SeeResult,
        alignment = Alignment.BottomCenter,
    ) {
        ItemsList(items = items.value)
    }
}

@Composable
internal fun ItemsList(items: List<String>) {
    Column {
        if (items.isEmpty()) {
            Text("No items")
        } else {
            items.forEach { item ->
                Text("• $item")
            }
        }
    }
}
