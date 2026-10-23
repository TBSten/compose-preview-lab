/*
 * Copyright (c) 2026 Composable Horizons — MIT License (see ui/THIRD_PARTY_NOTICES.md).
 * Adapted for Compose Preview Lab: PreviewLab* prefix, compose-unstyled 2.6.0, PreviewLabTheme facade colours.
 */
package me.tbsten.compose.preview.lab.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.composables.compose.ripple.rememberRippleIndication
import com.composeunstyled.CheckedIndicator
import com.composeunstyled.ProvideContentColor
import com.composeunstyled.StateIndicator
import com.composeunstyled.UnstyledCheckbox
import com.composeunstyled.UnstyledTriStateCheckbox
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.inverseRippleColor
import me.tbsten.compose.preview.lab.ui.util.thenIf

/**
 * A checkbox for independent binary selections.
 *
 * @param checked Whether the checkbox is currently checked.
 * @param onCheckedChange Called when the checked state changes.
 * @param modifier Modifier applied to the checkbox row.
 * @param enabled Whether the checkbox can be interacted with.
 * @param accessibilityLabel Accessible label announced for the checkbox.
 * @param interactionSource Interaction source used for focus and press state.
 * @param content Optional label or supporting content displayed next to the checkbox.
 */
@Composable
@UiComposePreviewLabApi
fun PreviewLabCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accessibilityLabel: String? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: (@Composable RowScope.() -> Unit)? = null,
) {
    val c = PreviewLabTheme.colors
    val boxShape = RoundedCornerShape(CheckboxCornerRadius)
    val backgroundColor = if (checked) c.primary else c.control
    val markColor = if (checked) c.onPrimary else Color.Transparent
    val borderColor = if (checked) c.primary else c.outline
    val activeIndication = if (checked) rememberRippleIndication(c.inverseRippleColor) else LocalIndication.current

    UnstyledCheckbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        accessibilityLabel = accessibilityLabel,
        interactionSource = interactionSource,
        indication = null,
        modifier = modifier.thenIf(!enabled) { alpha(CheckboxDisabledAlpha) },
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(CheckboxContentSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CheckedIndicator(
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .previewLabFocusRing(interactionSource = interactionSource, shape = boxShape)
                    .clip(boxShape)
                    .background(backgroundColor, boxShape)
                    .border(1.dp, borderColor, boxShape)
                    .size(CheckboxSize),
                indication = activeIndication,
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CheckMark(markColor) }
            }

            if (content != null) {
                ProvideContentColor(c.onPanel) { content() }
            }
        }
    }
}

/**
 * A checkbox that can represent on, off, or mixed selection state.
 *
 * @param state Current toggleable state shown by the checkbox.
 * @param onStateChange Called when the tri-state checkbox changes state.
 * @param modifier Modifier applied to the checkbox row.
 * @param enabled Whether the checkbox can be interacted with.
 * @param accessibilityLabel Accessible label announced for the checkbox.
 * @param interactionSource Interaction source used for focus and press state.
 * @param content Optional label or supporting content displayed next to the checkbox.
 */
@Composable
@UiComposePreviewLabApi
fun PreviewLabTriStateCheckbox(
    state: ToggleableState,
    onStateChange: (ToggleableState) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accessibilityLabel: String? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: (@Composable RowScope.() -> Unit)? = null,
) {
    val c = PreviewLabTheme.colors
    val checked = state != ToggleableState.Off
    val boxShape = RoundedCornerShape(CheckboxCornerRadius)
    val backgroundColor = if (checked) c.primary else c.control
    val markColor = if (checked) c.onPrimary else Color.Transparent
    val borderColor = if (checked) c.primary else c.outline
    val activeIndication = if (checked) rememberRippleIndication(c.inverseRippleColor) else LocalIndication.current

    UnstyledTriStateCheckbox(
        value = state,
        onClick = {
            onStateChange(
                when (state) {
                    ToggleableState.Off -> ToggleableState.On
                    ToggleableState.Indeterminate -> ToggleableState.On
                    ToggleableState.On -> ToggleableState.Off
                },
            )
        },
        enabled = enabled,
        accessibilityLabel = accessibilityLabel,
        interactionSource = interactionSource,
        indication = null,
        modifier = modifier.thenIf(!enabled) { alpha(CheckboxDisabledAlpha) },
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(CheckboxContentSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StateIndicator(
                modifier = Modifier
                    .previewLabFocusRing(interactionSource = interactionSource, shape = boxShape)
                    .clip(boxShape)
                    .background(backgroundColor, boxShape)
                    .border(1.dp, borderColor, boxShape)
                    .size(CheckboxSize),
                indication = activeIndication,
            ) { value ->
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    this@Row.AnimatedVisibility(
                        visible = value != ToggleableState.Off,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        if (value == ToggleableState.Indeterminate) {
                            Box(
                                Modifier
                                    .width(10.dp)
                                    .height(2.dp)
                                    .background(markColor, RoundedCornerShape(999.dp)),
                            )
                        } else {
                            CheckMark(markColor)
                        }
                    }
                }
            }

            if (content != null) {
                ProvideContentColor(c.onPanel) { content() }
            }
        }
    }
}

/** Composables UI check mark: two 2dp round-capped strokes inside a 14dp canvas. */
@Composable
private fun CheckMark(color: Color) {
    Canvas(Modifier.size(14.dp)) {
        val strokeWidth = 2.dp.toPx()
        drawLine(
            color,
            Offset(size.width * 0.2f, size.height * 0.52f),
            Offset(size.width * 0.42f, size.height * 0.74f),
            strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color,
            Offset(size.width * 0.42f, size.height * 0.74f),
            Offset(size.width * 0.8f, size.height * 0.28f),
            strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

private val CheckboxSize = 20.dp
private val CheckboxCornerRadius = 5.dp
private val CheckboxContentSpacing = 10.dp

/** Composables UI `disabledAlpha` token value (see Theme.kt `properties[alphas]`). */
private const val CheckboxDisabledAlpha = 0.33f
