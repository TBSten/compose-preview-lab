package me.tbsten.compose.preview.lab.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.composeunstyled.LocalContentColor
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Icon-only [PreviewLabIconButton] showing [painter] at 20dp, optionally wrapped in a [PreviewLabTooltipBox].
 *
 * @param painter Icon to show.
 * @param contentDescription Accessibility label of the icon. `null` (the default) also disables the tooltip.
 * @param onClick Called when the button is activated.
 * @param modifier Modifier applied to the button (an earlier `.size(...)` wins over [buttonSize]).
 * @param enabled Whether the button can be interacted with.
 * @param style Visual style used by the button.
 * @param buttonSize Size used for the square button dimension.
 * @param shape Shape used for the button container, border, and focus ring.
 * @param tint Icon tint; [Color.Unspecified] uses the button content colour.
 * @param tooltip Tooltip text shown on hover / long press; `null` shows none. Defaults to [contentDescription].
 * @param tooltipSide Side of the button the tooltip is placed on (header sites pass [PreviewLabTooltipSide.Bottom]).
 */
@Composable
@UiComposePreviewLabApi
fun PreviewLabIconButton(
    painter: Painter,
    contentDescription: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: PreviewLabButtonStyle = PreviewLabButtonStyle.Ghost,
    buttonSize: PreviewLabButtonSize = PreviewLabButtonSize.Small,
    shape: Shape = PreviewLabButtonDefaults.ButtonShape,
    tint: Color = Color.Unspecified,
    tooltip: String? = contentDescription,
    tooltipSide: PreviewLabTooltipSide = PreviewLabTooltipSide.Top,
) {
    val button: @Composable () -> Unit = {
        PreviewLabIconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            style = style,
            shape = shape,
            buttonSize = buttonSize,
        ) {
            PreviewLabIcon(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier.size(IconSize),
                tint = tint.takeOrElse { LocalContentColor.current },
            )
        }
    }
    if (tooltip == null) {
        button()
    } else {
        PreviewLabTooltipBox(tooltip = tooltip, tooltipSide = tooltipSide) { button() }
    }
}

/**
 * Icon-only [PreviewLabIconButton] showing [imageVector] at 20dp, optionally wrapped in a [PreviewLabTooltipBox].
 * See the [Painter] overload for the parameters.
 */
@Composable
@UiComposePreviewLabApi
fun PreviewLabIconButton(
    imageVector: ImageVector,
    contentDescription: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: PreviewLabButtonStyle = PreviewLabButtonStyle.Ghost,
    buttonSize: PreviewLabButtonSize = PreviewLabButtonSize.Small,
    shape: Shape = PreviewLabButtonDefaults.ButtonShape,
    tint: Color = Color.Unspecified,
    tooltip: String? = contentDescription,
    tooltipSide: PreviewLabTooltipSide = PreviewLabTooltipSide.Top,
) {
    val button: @Composable () -> Unit = {
        PreviewLabIconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            style = style,
            shape = shape,
            buttonSize = buttonSize,
        ) {
            PreviewLabIcon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                modifier = Modifier.size(IconSize),
                tint = tint.takeOrElse { LocalContentColor.current },
            )
        }
    }
    if (tooltip == null) {
        button()
    } else {
        PreviewLabTooltipBox(tooltip = tooltip, tooltipSide = tooltipSide) { button() }
    }
}

/**
 * Icon-only [PreviewLabIconButton] showing the drawable [resource] at 20dp, optionally wrapped in a
 * [PreviewLabTooltipBox]. See the [Painter] overload for the parameters.
 */
@Composable
@UiComposePreviewLabApi
fun PreviewLabIconButton(
    resource: DrawableResource,
    contentDescription: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: PreviewLabButtonStyle = PreviewLabButtonStyle.Ghost,
    buttonSize: PreviewLabButtonSize = PreviewLabButtonSize.Small,
    shape: Shape = PreviewLabButtonDefaults.ButtonShape,
    tint: Color = Color.Unspecified,
    tooltip: String? = contentDescription,
    tooltipSide: PreviewLabTooltipSide = PreviewLabTooltipSide.Top,
) = PreviewLabIconButton(
    painter = painterResource(resource),
    contentDescription = contentDescription,
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    style = style,
    buttonSize = buttonSize,
    shape = shape,
    tint = tint,
    tooltip = tooltip,
    tooltipSide = tooltipSide,
)

/** Icon size inside the resource icon buttons (Composables UI icon-button glyph size). */
private val IconSize = 20.dp
