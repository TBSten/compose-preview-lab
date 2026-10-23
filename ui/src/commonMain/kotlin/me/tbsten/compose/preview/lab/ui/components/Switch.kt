/*
 * Copyright (c) 2026 Composable Horizons — MIT License (see ui/THIRD_PARTY_NOTICES.md).
 * Adapted for Compose Preview Lab: PreviewLab* prefix, compose-unstyled 2.6.0, PreviewLabTheme facade colours.
 */
package me.tbsten.compose.preview.lab.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.FocusRingVisibility
import com.composeunstyled.ProvideContentColor
import com.composeunstyled.Thumb
import com.composeunstyled.Track
import com.composeunstyled.UnstyledSwitch
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.util.thenIfNotNull

/**
 * A binary switch for settings that change immediately.
 *
 * @param checked Whether the switch is currently on.
 * @param onCheckedChange Called when the checked state changes.
 * @param modifier Modifier applied to the switch row.
 * @param enabled Whether the switch can be interacted with.
 * @param accessibilityLabel Accessible label announced for the switch.
 * @param interactionSource Interaction source used for focus and press state.
 * @param content Optional label or supporting content displayed next to the switch.
 */
@Composable
@UiComposePreviewLabApi
fun PreviewLabSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accessibilityLabel: String? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: (@Composable RowScope.() -> Unit)? = null,
) {
    val c = PreviewLabTheme.colors
    val trackShape = RoundedCornerShape(999.dp)
    val trackColor by animateColorAsState(if (checked) c.switchSelectedTrack else c.switchTrack)

    // compose-unstyled 2.6.0 UnstyledSwitch has no accessibilityLabel parameter: apply the semantics here.
    UnstyledSwitch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .thenIfNotNull(accessibilityLabel) { label -> semantics { contentDescription = label } }
            .alpha(if (enabled) 1f else SwitchDisabledAlpha),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(SwitchContentSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Track(
                modifier = Modifier
                    .previewLabFocusRing(
                        interactionSource = interactionSource,
                        shape = trackShape,
                        visibility = FocusRingVisibility.Focused,
                    )
                    .background(trackColor, trackShape)
                    .border(Dp.Hairline, c.ring, trackShape)
                    .size(width = SwitchTrackWidth, height = SwitchTrackHeight)
                    .padding(SwitchTrackPadding),
            ) {
                Thumb(animationSpec = spring()) {
                    Box(
                        Modifier
                            .size(SwitchThumbSize)
                            .background(c.switchThumb, CircleShape)
                            .border(Dp.Hairline, c.ring, CircleShape),
                    )
                }
            }
            if (content != null) {
                ProvideContentColor(c.onPanel) { content() }
            }
        }
    }
}

private val SwitchTrackWidth = 44.dp
private val SwitchTrackHeight = 24.dp
private val SwitchTrackPadding = 2.dp
private val SwitchThumbSize = 20.dp
private val SwitchContentSpacing = 10.dp

/** Composables UI `disabledAlpha` token value (see Theme.kt `properties[alphas]`). */
private const val SwitchDisabledAlpha = 0.33f
