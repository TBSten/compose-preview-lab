/*
 * Copyright (c) 2026 Composable Horizons — MIT License (see ui/THIRD_PARTY_NOTICES.md).
 * Adapted for Compose Preview Lab: PreviewLab* prefix, compose-unstyled 2.6.0, PreviewLabTheme facade colours.
 */
package me.tbsten.compose.preview.lab.ui.components

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.composeunstyled.ProvideContentColor
import com.composeunstyled.SelectedIndicator
import com.composeunstyled.UnstyledRadioButton
import com.composeunstyled.UnstyledRadioGroup
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.util.thenIf

/**
 * A single-selection group that coordinates [PreviewLabRadio] options.
 *
 * @param value Currently selected value for the radio group.
 * @param onValueChange Called when the selected value changes.
 * @param modifier Modifier applied to the component.
 * @param accessibilityLabel Accessible label announced for the radio group.
 * @param content Composable content displayed by the component (the [PreviewLabRadio] options).
 */
@Composable
@UiComposePreviewLabApi
fun <T> PreviewLabRadioGroup(
    value: T?,
    onValueChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    accessibilityLabel: String? = null,
    content: @Composable () -> Unit,
) {
    UnstyledRadioGroup(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        accessibilityLabel = accessibilityLabel,
    ) {
        content()
    }
}

/**
 * A selectable option inside a [PreviewLabRadioGroup]. The label passed as [content] is merged into the radio's
 * semantics node, so the whole row is clickable and announced as one control.
 *
 * @param value The value represented by this radio.
 * @param modifier Modifier applied to the component.
 * @param enabled Whether the radio option can be interacted with.
 * @param interactionSource Interaction source used for focus and press state.
 * @param content Optional label or supporting content displayed next to the radio.
 */
@Composable
@UiComposePreviewLabApi
fun <T> PreviewLabRadio(
    value: T,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: (@Composable RowScope.() -> Unit)? = null,
) {
    val c = PreviewLabTheme.colors
    val rowShape = RoundedCornerShape(RadioRowCornerRadius)

    UnstyledRadioButton(
        value = value,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .clip(rowShape)
            .padding(RadioRowPadding)
            .thenIf(!enabled) { alpha(RadioDisabledAlpha) },
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(RadioContentSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SelectedIndicator(
                enter = fadeIn(),
                exit = fadeOut(),
                indication = LocalIndication.current,
                modifier = Modifier
                    .previewLabFocusRing(interactionSource = interactionSource, shape = CircleShape)
                    .clip(CircleShape)
                    .background(c.control, CircleShape)
                    .border(1.dp, c.outline, CircleShape)
                    .size(RadioSize),
            ) {
                Box(
                    modifier = Modifier.size(RadioSize),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        Modifier
                            .size(RadioDotSize)
                            .background(c.primary, CircleShape),
                    )
                }
            }
            if (content != null) {
                ProvideContentColor(c.onPanel) { content() }
            }
        }
    }
}

private val RadioSize = 20.dp
private val RadioDotSize = 10.dp
private val RadioRowCornerRadius = 8.dp
private val RadioRowPadding = 2.dp
private val RadioContentSpacing = 10.dp

/** Composables UI `disabledAlpha` token value (see Theme.kt `properties[alphas]`). */
private const val RadioDisabledAlpha = 0.33f
