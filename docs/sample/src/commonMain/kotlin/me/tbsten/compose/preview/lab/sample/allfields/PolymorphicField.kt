package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
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
import me.tbsten.compose.preview.lab.field.FixedField
import me.tbsten.compose.preview.lab.field.PolymorphicField
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.field.combined
import me.tbsten.compose.preview.lab.field.splitedOf
import me.tbsten.compose.preview.lab.previewlab.PreviewLab
import me.tbsten.compose.preview.lab.sample.OnValueChange
import me.tbsten.compose.preview.lab.sample.SpeechBubbleBox
import me.tbsten.compose.preview.lab.sample.speechBubble
import androidx.compose.ui.tooling.preview.Preview

internal sealed interface PolymorphicFieldUiState {
    data object Loading : PolymorphicFieldUiState
    data class Success(val data: String) : PolymorphicFieldUiState
    data class Error(val message: String) : PolymorphicFieldUiState
}

internal enum class PolymorphicFieldExampleSteps {
    EditValue,
    SeeResult,
}

/**
 * Demonstrates [PolymorphicField] for selecting between sealed class variants.
 *
 * Switch between Loading, Success, and Error UI states.
 * Each variant can have its own editable fields (e.g., Success has data, Error has message).
 */
@Preview
@ComposePreviewLabOption(id = "PolymorphicFieldExample")
@Composable
internal fun PolymorphicFieldExample() = PreviewLab {
    var step by remember { mutableStateOf(PolymorphicFieldExampleSteps.EditValue) }

    val uiState by fieldState {
        PolymorphicField(
            label = "UI State",
            initialValue = PolymorphicFieldUiState.Loading,
            fields = listOf(
                FixedField("Loading", PolymorphicFieldUiState.Loading),
                combined(
                    label = "Success",
                    field1 = StringField("Data", "Sample data"),
                    combine = { data -> PolymorphicFieldUiState.Success(data) },
                    split = { splitedOf(it.data) },
                ),
                combined(
                    label = "Error",
                    field1 = StringField("Message", "Something went wrong"),
                    combine = { message -> PolymorphicFieldUiState.Error(message) },
                    split = { splitedOf(it.message) },
                ),
            ),
        ).speechBubble(
            bubbleText = "1. Select UI state type",
            alignment = Alignment.BottomStart,
            visible = { step == PolymorphicFieldExampleSteps.EditValue },
        )
    }.also { state ->
        OnValueChange(state) {
            step = PolymorphicFieldExampleSteps.SeeResult
        }
    }

    SpeechBubbleBox(
        bubbleText = "2. UI State updated!",
        visible = step == PolymorphicFieldExampleSteps.SeeResult,
        alignment = Alignment.BottomCenter,
    ) {
        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.padding(32.dp),
        ) { uiState ->
            when (uiState) {
                is PolymorphicFieldUiState.Loading -> CircularProgressIndicator()
                is PolymorphicFieldUiState.Success -> Text(
                    "Success: ${(uiState).data}",
                    color = Color.Green,
                )
                is PolymorphicFieldUiState.Error -> Text(
                    "Error: ${(uiState).message}",
                    color = Color.Red,
                )
            }
        }
    }
}

sealed interface FixedFieldExampleUiState {
    data object Initial : FixedFieldExampleUiState
    data object Stable : FixedFieldExampleUiState
}

/**
 * Demonstrates [FixedField] for selecting from fixed, non-editable values.
 *
 * Unlike combined fields, [FixedField] has no editable sub-fields.
 * Useful for simple sealed objects or singleton states.
 */
@Preview
@ComposePreviewLabOption(id = "FixedFieldExample")
@Composable
internal fun FixedFieldExample() = PreviewLab {
    var step by remember { mutableStateOf(PolymorphicFieldExampleSteps.EditValue) }

    val value = fieldState {
        PolymorphicField(
            label = "Value",
            initialValue = FixedFieldExampleUiState.Initial,
            fields = listOf(
                FixedField("Fixed", FixedFieldExampleUiState.Initial),
                FixedField("Stable", FixedFieldExampleUiState.Stable),
            ),
        ).speechBubble(
            bubbleText = "No field content",
            alignment = Alignment.BottomStart,
            visible = { step == PolymorphicFieldExampleSteps.EditValue },
        )
    }

    SpeechBubbleBox(
        bubbleText = "2. Value selected!",
        visible = step == PolymorphicFieldExampleSteps.SeeResult,
        alignment = Alignment.BottomCenter,
    ) {
        Text("Value: ${value.value}")
    }
}
