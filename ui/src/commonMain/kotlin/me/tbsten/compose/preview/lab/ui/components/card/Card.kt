package me.tbsten.compose.preview.lab.ui.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.PreviewLabSurface

/**
 * A filled card: a [PreviewLabSurface] in [shape] painted with [colors], laying its content out in a [Column].
 *
 * @param colors Container/content colours; the card's content colour is always taken from here (no value lookup).
 */
@Composable
@UiComposePreviewLabApi
fun PreviewLabCard(
    modifier: Modifier = Modifier,
    shape: Shape = PreviewLabCardDefaults.Shape,
    colors: PreviewLabCardColors = PreviewLabCardDefaults.cardColors(),
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    PreviewLabSurface(
        modifier = modifier,
        shape = shape,
        color = colors.containerColor(enabled = true).value,
        contentColor = colors.contentColor(enabled = true).value,
        border = border,
    ) {
        Column(content = content)
    }
}

/** A clickable [PreviewLabCard] (theme ripple, focus ring, 33 % alpha when disabled). */
@Composable
@UiComposePreviewLabApi
fun PreviewLabCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = PreviewLabCardDefaults.Shape,
    colors: PreviewLabCardColors = PreviewLabCardDefaults.cardColors(),
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable ColumnScope.() -> Unit,
) {
    PreviewLabSurface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        color = colors.containerColor(enabled).value,
        contentColor = colors.contentColor(enabled).value,
        border = border,
        interactionSource = interactionSource,
    ) {
        Column(content = content)
    }
}

/** A card on the panel colour with a 1dp outline. */
@Composable
@UiComposePreviewLabApi
fun PreviewLabOutlinedCard(
    modifier: Modifier = Modifier,
    shape: Shape = PreviewLabCardDefaults.OutlinedShape,
    colors: PreviewLabCardColors = PreviewLabCardDefaults.outlinedCardColors(),
    border: BorderStroke = PreviewLabCardDefaults.outlinedCardBorder(),
    content: @Composable ColumnScope.() -> Unit,
) = PreviewLabCard(
    modifier = modifier,
    shape = shape,
    border = border,
    colors = colors,
    content = content,
)

/** A clickable [PreviewLabOutlinedCard]. */
@Composable
@UiComposePreviewLabApi
fun PreviewLabOutlinedCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = PreviewLabCardDefaults.OutlinedShape,
    colors: PreviewLabCardColors = PreviewLabCardDefaults.outlinedCardColors(),
    border: BorderStroke = PreviewLabCardDefaults.outlinedCardBorder(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable ColumnScope.() -> Unit,
) = PreviewLabCard(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    shape = shape,
    colors = colors,
    border = border,
    interactionSource = interactionSource,
    content = content,
)

/** Default values used by [PreviewLabCard] and [PreviewLabOutlinedCard]. */
@UiComposePreviewLabApi
object PreviewLabCardDefaults {
    /** Card corner shape (Composables `mediumShape`, 12dp). */
    val Shape: Shape @Composable get() = RoundedCornerShape(12.dp)

    /** Outlined card corner shape (same as [Shape]). */
    val OutlinedShape: Shape @Composable get() = Shape

    /** Outline width of [outlinedCardBorder]. */
    val BorderWidth = 1.dp

    /** Filled card colours: the control grey with `onSurface` content. */
    @Composable
    fun cardColors(
        containerColor: Color = PreviewLabTheme.colors.control,
        contentColor: Color = PreviewLabTheme.colors.onSurface,
        disabledContainerColor: Color = PreviewLabTheme.colors.disabled,
        disabledContentColor: Color = PreviewLabTheme.colors.onDisabled,
    ): PreviewLabCardColors = PreviewLabCardColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
    )

    /** Outlined card colours: the panel colour with `onPanel` content. */
    @Composable
    fun outlinedCardColors(
        containerColor: Color = PreviewLabTheme.colors.panel,
        contentColor: Color = PreviewLabTheme.colors.onPanel,
        disabledContainerColor: Color = PreviewLabTheme.colors.disabled,
        disabledContentColor: Color = PreviewLabTheme.colors.onDisabled,
    ): PreviewLabCardColors = PreviewLabCardColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
    )

    /** Outline of [PreviewLabOutlinedCard]: `colors.outline` (or `colors.disabled` when not [enabled]). */
    @Composable
    fun outlinedCardBorder(
        enabled: Boolean = true,
        color: Color = if (enabled) PreviewLabTheme.colors.outline else PreviewLabTheme.colors.disabled,
        borderWidth: Dp = BorderWidth,
    ): BorderStroke = remember(borderWidth, color) { BorderStroke(borderWidth, color) }
}

/** Container and content colours of a card in its enabled and disabled states. */
@Immutable
@UiComposePreviewLabApi
data class PreviewLabCardColors @UiComposePreviewLabApi constructor(
    private val containerColor: Color,
    private val contentColor: Color,
    private val disabledContainerColor: Color,
    private val disabledContentColor: Color,
) {
    @Composable
    @UiComposePreviewLabApi
    fun containerColor(enabled: Boolean): State<Color> =
        rememberUpdatedState(if (enabled) containerColor else disabledContainerColor)

    @Composable
    @UiComposePreviewLabApi
    fun contentColor(enabled: Boolean): State<Color> = rememberUpdatedState(if (enabled) contentColor else disabledContentColor)
}
