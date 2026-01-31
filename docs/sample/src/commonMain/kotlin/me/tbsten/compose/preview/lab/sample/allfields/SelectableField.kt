package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.sample.component.SamplePreviewLab
import me.tbsten.compose.preview.lab.field.SelectableField
import me.tbsten.compose.preview.lab.sample.OnValueChange
import me.tbsten.compose.preview.lab.sample.SpeechBubbleBox
import me.tbsten.compose.preview.lab.sample.speechBubble
import androidx.compose.ui.tooling.preview.Preview

internal enum class SelectableFieldExampleSteps {
    SelectValue,
    SeeResult,
}

/**
 * Demonstrates basic [SelectableField] with a list of string choices.
 *
 * Select a theme (Light, Dark, Auto) from the dropdown.
 * The app's color scheme changes based on the selection.
 */
@Preview
@ComposePreviewLabOption(id = "SelectableFieldExample")
@Composable
internal fun SelectableFieldExample() = SamplePreviewLab {
    var step by remember { mutableStateOf(SelectableFieldExampleSteps.SelectValue) }

    val theme = fieldState {
        SelectableField(
            label = "Theme",
            choices = listOf("Light", "Dark", "Auto"),
        ).speechBubble(
            bubbleText = "1. Select a theme",
            alignment = Alignment.BottomStart,
            visible = { step == SelectableFieldExampleSteps.SelectValue },
        )
    }.also { state ->
        OnValueChange(state) {
            step = SelectableFieldExampleSteps.SeeResult
        }
    }

    SelectableFieldMyApp(
        theme = theme.value,
        showBubble = step == SelectableFieldExampleSteps.SeeResult,
    )
}

/**
 * Demonstrates [SelectableField] with chip-style UI using [SelectableField.Type.CHIPS].
 *
 * Select a theme by tapping one of the chips.
 * Chips provide a compact, touch-friendly selection interface.
 */
@Preview
@ComposePreviewLabOption(id = "SelectableFieldChipsExample")
@Composable
internal fun SelectableFieldChipsExample() = SamplePreviewLab {
    var step by remember { mutableStateOf(SelectableFieldExampleSteps.SelectValue) }

    val theme = fieldState {
        SelectableField(
            label = "Theme",
            choices = listOf("Light", "Dark", "Auto"),
            type = SelectableField.Type.CHIPS,
        ).speechBubble(
            bubbleText = "1. Select a chip",
            alignment = Alignment.BottomStart,
            visible = { step == SelectableFieldExampleSteps.SelectValue },
        )
    }.also { state ->
        OnValueChange(state) {
            step = SelectableFieldExampleSteps.SeeResult
        }
    }

    SelectableFieldMyApp(
        theme = theme.value,
        showBubble = step == SelectableFieldExampleSteps.SeeResult,
    )
}

/**
 * Demonstrates [SelectableField] with a Map for custom display labels.
 *
 * The map allows different display text ("Light Mode") from actual values ("Light").
 * Useful when you need user-friendly labels for internal values.
 */
@Preview
@ComposePreviewLabOption(id = "SelectableFieldMapExample")
@Composable
internal fun SelectableFieldMapExample() = SamplePreviewLab {
    var step by remember { mutableStateOf(SelectableFieldExampleSteps.SelectValue) }

    val theme = fieldState {
        SelectableField(
            label = "Theme",
            choices = mapOf(
                "Light Mode" to "Light",
                "Dark Mode" to "Dark",
                "Auto (System)" to "Auto",
            ),
        ).speechBubble(
            bubbleText = "1. Select from map options",
            alignment = Alignment.BottomStart,
            visible = { step == SelectableFieldExampleSteps.SelectValue },
        )
    }.also { state ->
        OnValueChange(state) {
            step = SelectableFieldExampleSteps.SeeResult
        }
    }

    SelectableFieldMyApp(
        theme = theme.value,
        showBubble = step == SelectableFieldExampleSteps.SeeResult,
    )
}

/**
 * Demonstrates [SelectableField] using the builder DSL for fine-grained control.
 *
 * Use `choice()` to define each option with custom labels and default selection.
 * The builder pattern offers maximum flexibility for complex selection scenarios.
 */
@Preview
@ComposePreviewLabOption(id = "SelectableFieldBuilderExample")
@Composable
internal fun SelectableFieldBuilderExample() = SamplePreviewLab {
    var step by remember { mutableStateOf(SelectableFieldExampleSteps.SelectValue) }

    val theme = fieldState {
        SelectableField<String>(label = "Theme") {
            choice("Light", label = "Light Mode", isDefault = true)
            choice("Dark", label = "Dark Mode")
            choice("Auto", label = "Auto (System)")
        }.speechBubble(
            bubbleText = "1. Select using builder",
            alignment = Alignment.BottomStart,
            visible = { step == SelectableFieldExampleSteps.SelectValue },
        )
    }.also { state ->
        OnValueChange(state) {
            step = SelectableFieldExampleSteps.SeeResult
        }
    }

    SelectableFieldMyApp(
        theme = theme.value,
        showBubble = step == SelectableFieldExampleSteps.SeeResult,
    )
}

@Composable
internal fun SelectableFieldMyApp(
    theme: String,
    showBubble: Boolean = false,
) {
    MaterialTheme(
        colorScheme = when (theme) {
            "Light" -> lightColorScheme()
            "Dark" -> darkColorScheme()
            else -> if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
        },
    ) {
        Scaffold {
            SpeechBubbleBox(
                bubbleText = "2. Theme changed!",
                visible = showBubble,
                alignment = Alignment.BottomCenter,
            ) {
                Column(Modifier.padding(it)) {
                    Text("current theme: $theme")
                    Button(onClick = {}) {
                        Text("Button")
                    }
                }
            }
        }
    }
}
