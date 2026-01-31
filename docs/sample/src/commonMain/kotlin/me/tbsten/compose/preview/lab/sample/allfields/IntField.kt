package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.field.IntField
import me.tbsten.compose.preview.lab.field.NumberField
import me.tbsten.compose.preview.lab.field.withIncrementDecrementHint
import me.tbsten.compose.preview.lab.sample.OnValueChange
import me.tbsten.compose.preview.lab.sample.SpeechBubbleBox
import me.tbsten.compose.preview.lab.sample.component.SamplePreviewLab
import me.tbsten.compose.preview.lab.sample.speechBubble

internal enum class IntFieldExampleSteps {
    EditValue,
    SeeResult,
}

/**
 * Demonstrates basic [IntField] for editing integer values.
 *
 * Change the count value using the number input.
 * Supports increment/decrement buttons and direct text input.
 */
@Preview
@ComposePreviewLabOption(id = "IntFieldExample")
@Composable
internal fun IntFieldExample() = SamplePreviewLab {
    var step by remember { mutableStateOf(IntFieldExampleSteps.EditValue) }

    val count = fieldState {
        IntField("Count", 0)
            .withIncrementDecrementHint()
            .speechBubble(
                bubbleText = "1. Change the count",
                alignment = Alignment.BottomStart,
                visible = { step == IntFieldExampleSteps.EditValue },
            )
    }.also { state ->
        OnValueChange(state) {
            step = IntFieldExampleSteps.SeeResult
        }
    }

    SpeechBubbleBox(
        bubbleText = "2. Counter updated!",
        visible = step == IntFieldExampleSteps.SeeResult,
        alignment = Alignment.BottomCenter,
    ) {
        Counter(count = count.value)
    }
}

internal enum class IntFieldWithPrefixSuffixExampleSteps {
    EditValue,
    SeeResult,
}

/**
 * Demonstrates [IntField] with prefix/suffix using [NumberField.InputType.TextField].
 *
 * Shows "$" prefix and "yen" suffix to indicate currency context.
 * Useful when you need visual hints for numeric input.
 */
@Preview
@ComposePreviewLabOption(id = "IntFieldWithPrefixSuffixExample")
@Composable
internal fun IntFieldWithPrefixSuffixExample() = SamplePreviewLab {
    var step by remember { mutableStateOf(IntFieldWithPrefixSuffixExampleSteps.EditValue) }

    val count = fieldState {
        IntField(
            label = "Count",
            initialValue = 0,
            inputType = NumberField.InputType.TextField(
                prefix = { Text("$") },
                suffix = { Text("yen") },
            ),
        ).speechBubble(
            bubbleText = "1. Enter a value with prefix/suffix",
            alignment = Alignment.BottomStart,
            visible = { step == IntFieldWithPrefixSuffixExampleSteps.EditValue },
        )
    }.also { state ->
        OnValueChange(state) {
            step = IntFieldWithPrefixSuffixExampleSteps.SeeResult
        }
    }

    SpeechBubbleBox(
        bubbleText = "2. Value updated!",
        visible = step == IntFieldWithPrefixSuffixExampleSteps.SeeResult,
        alignment = Alignment.BottomCenter,
    ) {
        Counter(count = count.value)
    }
}

@Composable
internal fun Counter(count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Count: ",
            fontSize = 40.sp,
        )

        AnimatedContent(
            targetState = count,
            transitionSpec = {
                if (targetState > initialState) {
                    // 増加: 下から上へスライド + フェード
                    (fadeIn() + slideInVertically { height -> height }) togetherWith
                        (fadeOut() + slideOutVertically { height -> -height })
                } else {
                    // 減少: 上から下へスライド + フェード
                    (fadeIn() + slideInVertically { height -> -height }) togetherWith
                        (fadeOut() + slideOutVertically { height -> height })
                }
            },
            label = "CounterAnimation",
        ) { targetCount ->
            Text(
                text = "$targetCount",
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
