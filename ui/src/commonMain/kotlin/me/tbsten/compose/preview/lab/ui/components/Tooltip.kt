/*
 * Copyright (c) 2026 Composable Horizons — MIT License (see ui/THIRD_PARTY_NOTICES.md).
 * Adapted for Compose Preview Lab: PreviewLab* prefix, compose-unstyled 2.6.0, PreviewLabTheme facade colours.
 * PreviewLab addition: the thin `PreviewLabTooltipBox(tooltip, tooltipSide)` used by the resource icon buttons.
 */
package me.tbsten.compose.preview.lab.ui.components

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.composeunstyled.AnchorAlignment
import com.composeunstyled.AnchorSide
import com.composeunstyled.LocalTextStyle
import com.composeunstyled.ProvideContentColor
import com.composeunstyled.ProvideTextStyle
import com.composeunstyled.TooltipPanel as UnstyledTooltipPanel
import com.composeunstyled.TooltipScope as UnstyledTooltipScope
import com.composeunstyled.UnstyledTooltip
import kotlin.jvm.JvmInline
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme

private const val TooltipEnterDurationMillis = 300
private const val TooltipExitDurationMillis = 250

/** Receiver of the [PreviewLabTooltip] `panel` slot; call [PreviewLabTooltipPanel] on it. */
@UiComposePreviewLabApi
class PreviewLabTooltipScope internal constructor(
    internal val unstyledScope: UnstyledTooltipScope,
    internal val side: PreviewLabTooltipSide,
    internal val alignment: PreviewLabTooltipAlignment,
)

/** Side options for tooltip placement relative to its anchor. */
@UiComposePreviewLabApi
@JvmInline
value class PreviewLabTooltipSide internal constructor(@Suppress("unused") private val value: Int) {
    internal val anchorSide: AnchorSide
        get() = when (this) {
            Bottom -> AnchorSide.Bottom
            Start -> AnchorSide.Start
            End -> AnchorSide.End
            else -> AnchorSide.Top
        }

    @UiComposePreviewLabApi
    companion object {
        /** Places the tooltip above its anchor. */
        val Top = PreviewLabTooltipSide(0)

        /** Places the tooltip below its anchor. */
        val Bottom = PreviewLabTooltipSide(1)

        /** Places the tooltip before its anchor in the layout direction. */
        val Start = PreviewLabTooltipSide(2)

        /** Places the tooltip after its anchor in the layout direction. */
        val End = PreviewLabTooltipSide(3)
    }
}

/** Alignment options for tooltip placement along the anchor edge. */
@UiComposePreviewLabApi
@JvmInline
value class PreviewLabTooltipAlignment internal constructor(@Suppress("unused") private val value: Int) {
    internal val anchorAlignment: AnchorAlignment
        get() = when (this) {
            Start -> AnchorAlignment.Start
            End -> AnchorAlignment.End
            else -> AnchorAlignment.Center
        }

    @UiComposePreviewLabApi
    companion object {
        /** Aligns the tooltip to the start edge of the anchor. */
        val Start = PreviewLabTooltipAlignment(0)

        /** Centers the tooltip against the anchor. */
        val Center = PreviewLabTooltipAlignment(1)

        /** Aligns the tooltip to the end edge of the anchor. */
        val End = PreviewLabTooltipAlignment(2)
    }
}

/**
 * An anchored tooltip with configurable placement and timing.
 *
 * The panel renders through a compose-unstyled `Portal`, so a `TooltipHost` ancestor is required: `PreviewLabTheme`
 * installs one at the outermost theme, `PreviewLabModal` and `PreviewLabMenu` install their own around their
 * Dialog / Popup content. Placement is clamped into the host bounds and never flips, so anchors close to the top
 * of the window should pass [PreviewLabTooltipSide.Bottom].
 *
 * @param enabled Whether the tooltip can be shown.
 * @param side Side of the anchor where the tooltip should appear.
 * @param alignment Alignment of the tooltip along the chosen side.
 * @param sideOffset Distance between the tooltip and its anchor.
 * @param alignmentOffset Offset applied along the anchor edge.
 * @param longPressShowDurationMillis Duration to keep the tooltip visible after a long press.
 * @param hoverDelayMillis Delay before showing the tooltip on hover.
 * @param panel Composable tooltip panel content.
 * @param anchor Composable anchor that triggers the tooltip.
 */
@Composable
@UiComposePreviewLabApi
fun PreviewLabTooltip(
    enabled: Boolean = true,
    side: PreviewLabTooltipSide = PreviewLabTooltipSide.Top,
    alignment: PreviewLabTooltipAlignment = PreviewLabTooltipAlignment.Center,
    sideOffset: Dp = 8.dp,
    alignmentOffset: Dp = 0.dp,
    longPressShowDurationMillis: Long = 1500L,
    hoverDelayMillis: Long = 0L,
    panel: @Composable PreviewLabTooltipScope.() -> Unit,
    anchor: @Composable () -> Unit,
) {
    UnstyledTooltip(
        enabled = enabled,
        panel = {
            PreviewLabTooltipScope(
                unstyledScope = this,
                side = side,
                alignment = alignment,
            ).panel()
        },
        side = side.anchorSide,
        alignment = alignment.anchorAlignment,
        sideOffset = sideOffset,
        alignmentOffset = alignmentOffset,
        longPressShowDurationMillis = longPressShowDurationMillis,
        hoverDelayMillis = hoverDelayMillis,
        anchor = anchor,
    )
}

/**
 * A styled tooltip surface drawn near the anchor.
 *
 * @param modifier Modifier applied to the component.
 * @param shape Shape used for the tooltip panel.
 * @param backgroundColor Background color used for the tooltip panel.
 * @param contentColor Color used for tooltip content.
 * @param outlineColor Color of the 1dp border around the tooltip panel.
 * @param contentPadding Padding applied inside the tooltip panel.
 * @param enter Transition used when the tooltip appears.
 * @param exit Transition used when the tooltip disappears.
 * @param content Composable content displayed by the component.
 */
@Composable
@UiComposePreviewLabApi
fun PreviewLabTooltipScope.PreviewLabTooltipPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(6.dp),
    backgroundColor: Color = PreviewLabTheme.colors.panel,
    contentColor: Color = PreviewLabTheme.colors.onPanel,
    outlineColor: Color = PreviewLabTheme.colors.outline,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
    enter: EnterTransition? = null,
    exit: ExitTransition? = null,
    content: @Composable () -> Unit,
) {
    val transformOrigin = tooltipTransformOrigin(side, alignment)

    with(unstyledScope) {
        UnstyledTooltipPanel(
            modifier = modifier.zIndex(15f),
            enter = enter ?: tooltipEnterTransition(transformOrigin),
            exit = exit ?: tooltipExitTransition(transformOrigin),
        ) {
            Box(
                modifier = Modifier
                    .border(1.dp, outlineColor, shape)
                    .clip(shape)
                    .background(backgroundColor, shape)
                    .padding(contentPadding),
            ) {
                ProvideContentColor(contentColor) {
                    ProvideTextStyle(LocalTextStyle.current.merge(TooltipTextStyle)) { content() }
                }
            }
        }
    }
}

/**
 * PreviewLab convenience: shows [tooltip] in a [PreviewLabTooltipPanel] when [content] is hovered, long-pressed or
 * keyboard-focused. Used by the resource [PreviewLabIconButton] overloads.
 *
 * @param tooltip Text shown inside the tooltip panel.
 * @param modifier Modifier applied to the box wrapping [content] (the tooltip anchor).
 * @param enabled Whether the tooltip can be shown.
 * @param tooltipSide Side of the anchor the tooltip is placed on. Header sites pass [PreviewLabTooltipSide.Bottom]
 *   because placement clamps into the window instead of flipping.
 * @param content The anchor content.
 */
@Composable
@UiComposePreviewLabApi
fun PreviewLabTooltipBox(
    tooltip: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tooltipSide: PreviewLabTooltipSide = PreviewLabTooltipSide.Top,
    content: @Composable () -> Unit,
) {
    PreviewLabTooltip(
        enabled = enabled,
        side = tooltipSide,
        panel = {
            PreviewLabTooltipPanel {
                PreviewLabText(tooltip, style = PreviewLabTheme.typography.body3)
            }
        },
        anchor = { Box(modifier) { content() } },
    )
}

private val TooltipTextStyle = TextStyle()

private fun tooltipEnterTransition(transformOrigin: TransformOrigin): EnterTransition = scaleIn(
    animationSpec = tween(
        durationMillis = TooltipEnterDurationMillis,
        easing = LinearOutSlowInEasing,
    ),
    initialScale = 0.96f,
    transformOrigin = transformOrigin,
) + fadeIn(tween(durationMillis = TooltipEnterDurationMillis))

private fun tooltipExitTransition(transformOrigin: TransformOrigin): ExitTransition = scaleOut(
    animationSpec = tween(durationMillis = TooltipExitDurationMillis),
    targetScale = 0.96f,
    transformOrigin = transformOrigin,
) + fadeOut(tween(durationMillis = TooltipExitDurationMillis))

private fun tooltipTransformOrigin(side: PreviewLabTooltipSide, alignment: PreviewLabTooltipAlignment): TransformOrigin =
    when (side) {
        PreviewLabTooltipSide.Top -> TransformOrigin(
            pivotFractionX = alignment.transformOriginFraction,
            pivotFractionY = 1f,
        )
        PreviewLabTooltipSide.Bottom -> TransformOrigin(
            pivotFractionX = alignment.transformOriginFraction,
            pivotFractionY = 0f,
        )
        PreviewLabTooltipSide.Start -> TransformOrigin(
            pivotFractionX = 1f,
            pivotFractionY = alignment.transformOriginFraction,
        )
        else -> TransformOrigin(
            pivotFractionX = 0f,
            pivotFractionY = alignment.transformOriginFraction,
        )
    }

private val PreviewLabTooltipAlignment.transformOriginFraction: Float
    get() = when (this) {
        PreviewLabTooltipAlignment.Start -> 0f
        PreviewLabTooltipAlignment.Center -> 0.5f
        else -> 1f
    }
