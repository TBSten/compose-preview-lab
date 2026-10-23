/*
 * Copyright (c) 2026 Composable Horizons — MIT License (see ui/THIRD_PARTY_NOTICES.md).
 * Adapted for Compose Preview Lab: PreviewLab* prefix, compose-unstyled 2.6.0, PreviewLabTheme facade colours.
 */
package me.tbsten.compose.preview.lab.ui.components

import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composables.compose.ripple.rememberRippleIndication
import com.composeunstyled.LocalContentColor
import com.composeunstyled.LocalTextStyle
import com.composeunstyled.ProvideContentColor
import com.composeunstyled.ProvideTextStyle
import com.composeunstyled.UnstyledButton
import com.composeunstyled.buildModifier
import kotlin.jvm.JvmInline
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.inverseRippleColor

/** Visual style variants for [PreviewLabButton] and [PreviewLabIconButton]. */
@UiComposePreviewLabApi
@JvmInline
value class PreviewLabButtonStyle internal constructor(@Suppress("unused") private val value: Int) {
    @UiComposePreviewLabApi
    companion object {
        /** Use for the primary action on a screen. */
        val Primary = PreviewLabButtonStyle(0)

        /** Use for secondary actions. */
        val Secondary = PreviewLabButtonStyle(1)

        /** Use for lower-emphasis actions with an outline. */
        val Outlined = PreviewLabButtonStyle(2)

        /** Use for destructive actions. */
        val Destructive = PreviewLabButtonStyle(3)

        /** Use for low-emphasis actions without a container. */
        val Ghost = PreviewLabButtonStyle(4)

        /** Style for clickable text links. */
        val Link = PreviewLabButtonStyle(5)

        /**
         * The default button style.
         *
         * PreviewLab deviation from Composables UI (whose default is Secondary): PreviewLab's `text =` buttons are
         * primary actions.
         */
        val Default = Primary
    }
}

/** Size variants for [PreviewLabButton] and [PreviewLabIconButton]. */
@UiComposePreviewLabApi
@JvmInline
value class PreviewLabButtonSize internal constructor(@Suppress("unused") private val value: Int) {
    @UiComposePreviewLabApi
    companion object {
        /** Small button size (32dp). */
        val Small = PreviewLabButtonSize(0)

        /** Regular button size (36dp). */
        val Regular = PreviewLabButtonSize(1)

        /** Large button size (40dp). */
        val Large = PreviewLabButtonSize(2)

        /** The default button size. */
        val Default = Regular
    }
}

/** Default values used by [PreviewLabButton] and [PreviewLabIconButton]. */
@UiComposePreviewLabApi
object PreviewLabButtonDefaults {
    /**
     * Composables UI pointer button shape.
     * Must stay a [RoundedCornerShape] (PreviewLabHeader.web.kt copies its corners for the split button).
     */
    val ButtonShape: RoundedCornerShape = RoundedCornerShape(10.dp)

    /**
     * Regular-size padding.
     * Must stay a [PaddingValues] (PreviewLabHeader.web.kt reads `calculateTopPadding()`).
     */
    val contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp)

    /** Height of a [PreviewLabButtonSize.Regular] button. */
    val MinHeight: Dp = 36.dp

    /** Border width of a [PreviewLabButtonStyle.Outlined] button. */
    val OutlineHeight: Dp = 1.dp

    /** Button height (and square icon-button dimension) for [size]. */
    fun heightFor(size: PreviewLabButtonSize): Dp = when (size) {
        PreviewLabButtonSize.Small -> 32.dp
        PreviewLabButtonSize.Large -> 40.dp
        else -> MinHeight
    }

    /** Content padding for [size] and [style]; [PreviewLabButtonStyle.Link] buttons have no padding. */
    fun contentPaddingFor(size: PreviewLabButtonSize, style: PreviewLabButtonStyle): PaddingValues =
        if (style == PreviewLabButtonStyle.Link) {
            PaddingValues(0.dp)
        } else {
            PaddingValues(
                horizontal = when (size) {
                    PreviewLabButtonSize.Small -> 12.dp
                    PreviewLabButtonSize.Large -> 20.dp
                    else -> 16.dp
                },
                vertical = 8.dp,
            )
        }

    /** Container shape for [style]: a small radius for [PreviewLabButtonStyle.Link], [ButtonShape] otherwise. */
    fun shapeFor(style: PreviewLabButtonStyle): Shape =
        if (style == PreviewLabButtonStyle.Link) RoundedCornerShape(6.dp) else ButtonShape
}

/**
 * A component that can be clicked.
 *
 * @param onClick Called when the button is activated.
 * @param modifier Modifier applied to the button.
 * @param enabled Whether the button can be interacted with.
 * @param selected PreviewLab addition: paints Secondary/Outlined/Ghost buttons with the `selectedControl` colours.
 *   Ignored for Primary/Destructive/Link.
 * @param style Visual style used by the button.
 * @param shape Shape used for the button container, border, and focus ring.
 * @param buttonSize Size used for button height and padding.
 * @param contentPadding Padding applied inside the button bounds, around the button content.
 * @param borderWidth Width of the button border when the selected style draws one.
 * @param interactionSource Interaction source used for focus, press, hover, and indication state.
 * @param indication Indication drawn on press/hover; `null` draws none.
 * @param contentColor Colour provided to the content through `LocalContentColor`.
 * @param content The content to display within the button.
 */
@Composable
@UiComposePreviewLabApi
fun PreviewLabButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    style: PreviewLabButtonStyle = PreviewLabButtonStyle.Default,
    shape: Shape = PreviewLabButtonDefaults.shapeFor(style),
    buttonSize: PreviewLabButtonSize = PreviewLabButtonSize.Default,
    contentPadding: PaddingValues = PreviewLabButtonDefaults.contentPaddingFor(buttonSize, style),
    borderWidth: Dp = PreviewLabButtonDefaults.OutlineHeight,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    indication: Indication? = buttonIndicationFor(style),
    contentColor: Color = buttonContentColorFor(style, selected),
    content: @Composable RowScope.() -> Unit,
) {
    val hovered by interactionSource.collectIsHoveredAsState()
    BaseButton(
        onClick = onClick,
        modifier = if (style == PreviewLabButtonStyle.Link) {
            modifier
        } else {
            modifier.heightIn(min = PreviewLabButtonDefaults.heightFor(buttonSize))
        },
        enabled = enabled,
        style = style,
        backgroundColor = buttonBackgroundColorFor(style, selected),
        contentColor = contentColor,
        shape = shape,
        contentPadding = contentPadding,
        borderColor = buttonBorderColorFor(style),
        borderWidth = borderWidth,
        interactionSource = interactionSource,
        indication = indication,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProvideTextStyle(LocalTextStyle.current.merge(buttonLabelTextStyleFor(style, hovered))) {
                content()
            }
        }
    }
}

/**
 * PreviewLab convenience over [PreviewLabButton] that renders [text] as the button label.
 *
 * @param text Label shown inside the button.
 * @param onClick Called when the button is activated.
 * @param modifier Modifier applied to the button.
 * @param enabled Whether the button can be interacted with.
 * @param selected See [PreviewLabButton].
 * @param style Visual style used by the button.
 * @param buttonSize Size used for button height and padding.
 * @param shape Shape used for the button container, border, and focus ring.
 * @param contentPadding Padding applied inside the button bounds, around the label.
 */
@Composable
@UiComposePreviewLabApi
fun PreviewLabButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    style: PreviewLabButtonStyle = PreviewLabButtonStyle.Default,
    buttonSize: PreviewLabButtonSize = PreviewLabButtonSize.Default,
    shape: Shape = PreviewLabButtonDefaults.shapeFor(style),
    contentPadding: PaddingValues = PreviewLabButtonDefaults.contentPaddingFor(buttonSize, style),
) = PreviewLabButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    selected = selected,
    style = style,
    shape = shape,
    buttonSize = buttonSize,
    contentPadding = contentPadding,
) {
    PreviewLabText(text)
}

/**
 * Variant of [PreviewLabButton] meant for square targets, such as icons.
 *
 * @param onClick Called when the button is activated.
 * @param modifier Modifier applied to the button.
 * @param enabled Whether the button can be interacted with.
 * @param style Visual style used by the button.
 * @param shape Shape used for the button container, border, and focus ring.
 * @param buttonSize Size used for the square button dimension.
 * @param borderWidth Width of the button border when the selected style draws one.
 * @param interactionSource Interaction source used for focus, press, hover, and indication state.
 * @param indication Indication drawn on press/hover; `null` draws none.
 * @param contentColor Colour provided to the content through `LocalContentColor`.
 * @param content Composable content displayed inside the button.
 */
@Composable
@UiComposePreviewLabApi
fun PreviewLabIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: PreviewLabButtonStyle = PreviewLabButtonStyle.Default,
    shape: Shape = PreviewLabButtonDefaults.ButtonShape,
    buttonSize: PreviewLabButtonSize = PreviewLabButtonSize.Default,
    borderWidth: Dp = PreviewLabButtonDefaults.OutlineHeight,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    indication: Indication? = buttonIndicationFor(style),
    contentColor: Color = buttonContentColorFor(style, selected = false),
    content: @Composable () -> Unit,
) = BaseButton(
    onClick = onClick,
    // callers' earlier `.size(20.dp)` still wins (modifier order)
    modifier = modifier.size(PreviewLabButtonDefaults.heightFor(buttonSize)),
    enabled = enabled,
    style = style,
    backgroundColor = buttonBackgroundColorFor(style, selected = false),
    contentColor = contentColor,
    shape = shape,
    contentPadding = PaddingValues(0.dp),
    borderColor = buttonBorderColorFor(style),
    borderWidth = borderWidth,
    interactionSource = interactionSource,
    indication = indication,
    content = content,
)

@Composable
private fun BaseButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    style: PreviewLabButtonStyle,
    backgroundColor: Color,
    contentColor: Color,
    shape: Shape,
    contentPadding: PaddingValues,
    borderColor: Color,
    borderWidth: Dp,
    interactionSource: MutableInteractionSource,
    indication: Indication?,
    content: @Composable () -> Unit,
) {
    val alpha = if (enabled) 1f else DisabledAlpha
    UnstyledButton(
        onClick = onClick,
        enabled = enabled,
        contentPadding = contentPadding,
        modifier = modifier
            .bouncyPress(interactionSource = interactionSource, enabled = enabled)
            .previewLabFocusRing(interactionSource = interactionSource, shape = shape)
            .graphicsLayer { this.alpha = alpha }
            .then(
                buildModifier {
                    if (style != PreviewLabButtonStyle.Link) {
                        add(Modifier.clip(shape))
                        add(Modifier.background(backgroundColor, shape))
                        if (borderColor.isSpecified && borderColor != Color.Transparent && borderWidth > 0.dp) {
                            add(Modifier.border(borderWidth, borderColor, shape))
                        }
                    }
                },
            ),
        interactionSource = interactionSource,
        indication = indication,
    ) {
        ProvideContentColor(contentColor) { content() }
    }
}

/** Composables UI `disabledAlpha`. */
private const val DisabledAlpha = 0.33f

/** Styles that honour `selected` (PreviewLab addition, e.g. gallery NodeView rows). */
private val SelectableStyles = setOf(
    PreviewLabButtonStyle.Secondary,
    PreviewLabButtonStyle.Outlined,
    PreviewLabButtonStyle.Ghost,
)

@Composable
private fun buttonBackgroundColorFor(style: PreviewLabButtonStyle, selected: Boolean): Color {
    val c = PreviewLabTheme.colors
    if (selected && style in SelectableStyles) return c.selectedControl
    return when (style) {
        PreviewLabButtonStyle.Primary -> c.primary
        PreviewLabButtonStyle.Secondary -> c.secondary
        PreviewLabButtonStyle.Outlined -> c.panel
        PreviewLabButtonStyle.Destructive -> c.error
        else -> Color.Transparent
    }
}

@Composable
private fun buttonContentColorFor(style: PreviewLabButtonStyle, selected: Boolean): Color {
    val c = PreviewLabTheme.colors
    if (selected && style in SelectableStyles) return c.onSelectedControl
    return when (style) {
        PreviewLabButtonStyle.Primary -> c.onPrimary
        PreviewLabButtonStyle.Secondary -> c.onSecondary
        PreviewLabButtonStyle.Destructive -> c.onError
        PreviewLabButtonStyle.Outlined -> c.onPanel
        PreviewLabButtonStyle.Link -> c.primary
        else -> LocalContentColor.current
    }
}

@Composable
private fun buttonBorderColorFor(style: PreviewLabButtonStyle): Color =
    if (style == PreviewLabButtonStyle.Outlined) PreviewLabTheme.colors.outline else Color.Unspecified

@Composable
private fun buttonIndicationFor(style: PreviewLabButtonStyle): Indication? = when (style) {
    PreviewLabButtonStyle.Primary, PreviewLabButtonStyle.Destructive ->
        rememberRippleIndication(PreviewLabTheme.colors.inverseRippleColor)
    PreviewLabButtonStyle.Link -> null
    else -> LocalIndication.current // theme default ripple
}

@Composable
private fun buttonLabelTextStyleFor(style: PreviewLabButtonStyle, hovered: Boolean): TextStyle {
    val base = PreviewLabTheme.typography.button // Medium 14/20
    return if (style == PreviewLabButtonStyle.Link && hovered) {
        base.copy(textDecoration = TextDecoration.Underline)
    } else {
        base
    }
}
