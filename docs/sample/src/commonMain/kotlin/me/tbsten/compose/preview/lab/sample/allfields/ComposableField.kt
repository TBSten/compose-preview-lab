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
import me.tbsten.compose.preview.lab.sample.component.SamplePreviewLab
import me.tbsten.compose.preview.lab.field.ComposableField
import me.tbsten.compose.preview.lab.field.ComposableFieldValue
import me.tbsten.compose.preview.lab.sample.SpeechBubbleBox
import androidx.compose.ui.tooling.preview.Preview

/**
 * Demonstrates [ComposableField] for switching between composable content.
 *
 * Select different composable implementations (Red box, Text, Empty)
 * to test how your container handles various child content.
 */
@Preview
@ComposePreviewLabOption(id = "ComposableFieldExample")
@Composable
internal fun ComposableFieldExample() = SamplePreviewLab {
    var showBubble by remember { mutableStateOf(true) }

    SpeechBubbleBox(
        bubbleText = "Select composable content from the field",
        visible = showBubble,
        onClose = { showBubble = false },
        alignment = Alignment.BottomCenter,
    ) {
        ComposableFieldMyContainer(
            content = fieldValue {
                ComposableField(
                    label = "Content",
                    initialValue = ComposableFieldValue.Red32X32,
                )
            },
        )
    }
}

/**
 * Demonstrates [ComposableField] with predefined choices.
 *
 * Choose from a curated list of composable content options.
 * Restricts selection to only the predefined values for consistency.
 */
@Preview
@ComposePreviewLabOption(id = "ComposableFieldWithPredefinedValuesExample")
@Composable
internal fun ComposableFieldWithPredefinedValuesExample() = SamplePreviewLab {
    var showBubble by remember { mutableStateOf(true) }

    SpeechBubbleBox(
        bubbleText = "Choose from presets in the field",
        visible = showBubble,
        onClose = { showBubble = false },
        alignment = Alignment.BottomCenter,
    ) {
        ComposableFieldMyContainer(
            content = fieldValue {
                ComposableField(
                    label = "Content",
                    initialValue = ComposableFieldValue.Red32X32,
                    choices = listOf(
                        ComposableFieldValue.Red32X32,
                        ComposableFieldValue.SimpleText,
                        ComposableFieldValue.Empty,
                    ),
                )
            },
        )
    }
}

@Composable
internal fun ComposableFieldMyContainer(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(150.dp)
            .background(Color.LightGray),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
