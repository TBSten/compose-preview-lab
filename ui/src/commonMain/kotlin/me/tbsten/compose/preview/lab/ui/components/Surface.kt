package me.tbsten.compose.preview.lab.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.ProvideContentColor
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.contentColorFor

/**
 * A plain painted container: background [color] in [shape], optional [border] and shadow, and the content colour
 * provided to its children.
 *
 * @param color Background colour (default `colors.surface`, the control grey; panels pass `colors.panel`).
 * @param contentColor Content colour for children. Defaults to the palette pair of [color]; a non-palette colour
 * (Transparent, alpha tints, animated colours) inherits the surrounding content colour.
 */
@Composable
@NonRestartableComposable
@UiComposePreviewLabApi
fun PreviewLabSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    color: Color = PreviewLabTheme.colors.surface,
    contentColor: Color = contentColorFor(color),
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    content: @Composable () -> Unit,
) {
    ProvideContentColor(contentColor) {
        Box(
            modifier = modifier
                .surface(
                    shape = shape,
                    backgroundColor = color,
                    border = border,
                    shadowElevation = shadowElevation,
                )
                .semantics(mergeDescendants = false) {
                    isTraversalGroup = true
                }
                .pointerInput(Unit) {},
            propagateMinConstraints = true,
        ) {
            content()
        }
    }
}

/**
 * A clickable [PreviewLabSurface]: theme ripple, keyboard focus ring in [shape], 33 % alpha when disabled.
 *
 * @param color Background colour (default `colors.background`).
 * @param contentColor Content colour for children; same default rule as the plain overload.
 */
@Composable
@NonRestartableComposable
@UiComposePreviewLabApi
fun PreviewLabSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RectangleShape,
    color: Color = PreviewLabTheme.colors.background,
    contentColor: Color = contentColorFor(color),
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit,
) {
    val alpha = if (enabled) 1f else DisabledAlpha
    ProvideContentColor(contentColor) {
        Box(
            modifier = modifier
                .previewLabFocusRing(interactionSource = interactionSource, shape = shape)
                .graphicsLayer { this.alpha = alpha }
                .surface(
                    shape = shape,
                    backgroundColor = color,
                    border = border,
                    shadowElevation = shadowElevation,
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    enabled = enabled,
                    onClick = onClick,
                ),
            propagateMinConstraints = true,
        ) {
            content()
        }
    }
}

@Composable
private fun Modifier.surface(shape: Shape, backgroundColor: Color, border: BorderStroke?, shadowElevation: Dp) = this
    .shadow(
        ambientColor = PreviewLabTheme.colors.elevation,
        spotColor = PreviewLabTheme.colors.elevation,
        elevation = shadowElevation,
        shape = shape,
        clip = false,
    )
    .then(if (border != null) Modifier.border(border, shape) else Modifier)
    .background(color = backgroundColor, shape = shape)
    .clip(shape)

/** Composables UI `disabledAlpha`. */
private const val DisabledAlpha = 0.33f
