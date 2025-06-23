package me.tbsten.compose.preview.lab.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.LocalContentColor
import me.tbsten.compose.preview.lab.ui.contentColorFor
import me.tbsten.compose.preview.lab.ui.foundation.ButtonElevation
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun IconButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    variant: IconButtonVariant = IconButtonVariant.Primary,
    shape: Shape = IconButtonDefaults.ButtonSquareShape,
    onClick: () -> Unit = {},
    contentPadding: PaddingValues = IconButtonDefaults.ButtonPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit,
) {
    val style = IconButtonDefaults.styleFor(variant, shape)

    IconButtonComponent(
        modifier = modifier,
        enabled = enabled,
        loading = loading,
        style = style,
        onClick = onClick,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content,
    )
}

@Composable
private fun IconButtonComponent(
    modifier: Modifier,
    enabled: Boolean,
    loading: Boolean,
    style: IconButtonStyle,
    onClick: () -> Unit,
    contentPadding: PaddingValues,
    interactionSource: MutableInteractionSource,
    content: @Composable () -> Unit,
) {
    val containerColor = style.colors.containerColor(enabled).value
    val contentColor = style.colors.contentColor(enabled).value
    val borderColor = style.colors.borderColor(enabled).value
    val borderStroke = if (borderColor != null) BorderStroke(IconButtonDefaults.OutlineHeight, borderColor) else null

    val shadowElevation = style.elevation?.shadowElevation(enabled, interactionSource)?.value ?: 0.dp

    Surface(
        onClick = onClick,
        modifier =
            modifier.defaultMinSize(
                minWidth = IconButtonDefaults.ButtonSize,
                minHeight = IconButtonDefaults.ButtonSize,
            ).semantics { role = Role.Button },
        enabled = enabled,
        shape = style.shape,
        color = containerColor,
        contentColor = contentColor,
        border = borderStroke,
        shadowElevation = shadowElevation,
        interactionSource = interactionSource,
    ) {
        Box(
            modifier = Modifier.padding(contentPadding),
            contentAlignment = Alignment.Center,
        ) {
            // Add a loading indicator if needed
            content()
        }
    }
}

enum class IconButtonVariant {
    Primary,
    PrimaryOutlined,
    PrimaryElevated,
    PrimaryGhost,
    Secondary,
    SecondaryOutlined,
    SecondaryElevated,
    SecondaryGhost,
    Destructive,
    DestructiveOutlined,
    DestructiveElevated,
    DestructiveGhost,
    Ghost,
}

internal object IconButtonDefaults {
    val ButtonSize = 44.dp
    val ButtonPadding = PaddingValues(4.dp)
    val ButtonSquareShape = RoundedCornerShape(12.dp)
    val ButtonCircleShape = RoundedCornerShape(percent = 50)
    val OutlineHeight = 1.dp

    @Composable
    fun buttonElevation() =
        ButtonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 2.dp,
            focusedElevation = 2.dp,
            hoveredElevation = 2.dp,
            disabledElevation = 0.dp,
        )

    @Composable
    fun styleFor(variant: IconButtonVariant, shape: Shape): IconButtonStyle {
        return when (variant) {
            IconButtonVariant.Primary -> primaryFilled(shape)
            IconButtonVariant.PrimaryOutlined -> primaryOutlined(shape)
            IconButtonVariant.PrimaryElevated -> primaryElevated(shape)
            IconButtonVariant.PrimaryGhost -> primaryGhost(shape)
            IconButtonVariant.Secondary -> secondaryFilled(shape)
            IconButtonVariant.SecondaryOutlined -> secondaryOutlined(shape)
            IconButtonVariant.SecondaryElevated -> secondaryElevated(shape)
            IconButtonVariant.SecondaryGhost -> secondaryGhost(shape)
            IconButtonVariant.Destructive -> destructiveFilled(shape)
            IconButtonVariant.DestructiveOutlined -> destructiveOutlined(shape)
            IconButtonVariant.DestructiveElevated -> destructiveElevated(shape)
            IconButtonVariant.DestructiveGhost -> destructiveGhost(shape)
            IconButtonVariant.Ghost -> ghost(shape)
        }
    }

    @Composable
    fun primaryFilled(shape: Shape) =
        IconButtonStyle(
            colors =
                IconButtonColors(
                    containerColor = PreviewLabTheme.colors.primary,
                    contentColor = PreviewLabTheme.colors.onPrimary,
                    disabledContainerColor = PreviewLabTheme.colors.disabled,
                    disabledContentColor = PreviewLabTheme.colors.onDisabled,
                ),
            shape = shape,
            elevation = null,
        )

    @Composable
    fun primaryOutlined(shape: Shape) =
        IconButtonStyle(
            colors =
                IconButtonColors(
                    containerColor = PreviewLabTheme.colors.transparent,
                    contentColor = PreviewLabTheme.colors.primary,
                    borderColor = PreviewLabTheme.colors.primary,
                    disabledContainerColor = PreviewLabTheme.colors.transparent,
                    disabledContentColor = PreviewLabTheme.colors.onDisabled,
                    disabledBorderColor = PreviewLabTheme.colors.disabled,
                ),
            shape = shape,
            elevation = null,
        )

    @Composable
    fun primaryElevated(shape: Shape) =
        IconButtonStyle(
            colors =
                IconButtonColors(
                    containerColor = PreviewLabTheme.colors.primary,
                    contentColor = PreviewLabTheme.colors.onPrimary,
                    disabledContainerColor = PreviewLabTheme.colors.disabled,
                    disabledContentColor = PreviewLabTheme.colors.onDisabled,
                ),
            shape = shape,
            elevation = buttonElevation(),
        )

    @Composable
    fun primaryGhost(shape: Shape) =
        IconButtonStyle(
            colors =
                IconButtonColors(
                    containerColor = PreviewLabTheme.colors.transparent,
                    contentColor = PreviewLabTheme.colors.primary,
                    borderColor = PreviewLabTheme.colors.transparent,
                    disabledContainerColor = PreviewLabTheme.colors.transparent,
                    disabledContentColor = PreviewLabTheme.colors.onDisabled,
                    disabledBorderColor = PreviewLabTheme.colors.transparent,
                ),
            shape = shape,
            elevation = null,
        )

    @Composable
    fun secondaryFilled(shape: Shape) =
        IconButtonStyle(
            colors =
                IconButtonColors(
                    containerColor = PreviewLabTheme.colors.secondary,
                    contentColor = PreviewLabTheme.colors.onSecondary,
                    disabledContainerColor = PreviewLabTheme.colors.disabled,
                    disabledContentColor = PreviewLabTheme.colors.onDisabled,
                ),
            shape = shape,
            elevation = null,
        )

    @Composable
    fun secondaryOutlined(shape: Shape) =
        IconButtonStyle(
            colors =
                IconButtonColors(
                    containerColor = PreviewLabTheme.colors.transparent,
                    contentColor = PreviewLabTheme.colors.secondary,
                    borderColor = PreviewLabTheme.colors.secondary,
                    disabledContainerColor = PreviewLabTheme.colors.transparent,
                    disabledContentColor = PreviewLabTheme.colors.onDisabled,
                    disabledBorderColor = PreviewLabTheme.colors.disabled,
                ),
            shape = shape,
            elevation = null,
        )

    @Composable
    fun secondaryElevated(shape: Shape) =
        IconButtonStyle(
            colors =
                IconButtonColors(
                    containerColor = PreviewLabTheme.colors.secondary,
                    contentColor = PreviewLabTheme.colors.onSecondary,
                    disabledContainerColor = PreviewLabTheme.colors.disabled,
                    disabledContentColor = PreviewLabTheme.colors.onDisabled,
                ),
            shape = shape,
            elevation = buttonElevation(),
        )

    @Composable
    fun secondaryGhost(shape: Shape) =
        IconButtonStyle(
            colors =
                IconButtonColors(
                    containerColor = PreviewLabTheme.colors.transparent,
                    contentColor = PreviewLabTheme.colors.secondary,
                    borderColor = PreviewLabTheme.colors.transparent,
                    disabledContainerColor = PreviewLabTheme.colors.transparent,
                    disabledContentColor = PreviewLabTheme.colors.onDisabled,
                    disabledBorderColor = PreviewLabTheme.colors.transparent,
                ),
            shape = shape,
            elevation = null,
        )

    @Composable
    fun destructiveFilled(shape: Shape) =
        IconButtonStyle(
            colors =
                IconButtonColors(
                    containerColor = PreviewLabTheme.colors.error,
                    contentColor = PreviewLabTheme.colors.onError,
                    disabledContainerColor = PreviewLabTheme.colors.disabled,
                    disabledContentColor = PreviewLabTheme.colors.onDisabled,
                ),
            shape = shape,
            elevation = null,
        )

    @Composable
    fun destructiveOutlined(shape: Shape) =
        IconButtonStyle(
            colors =
                IconButtonColors(
                    containerColor = PreviewLabTheme.colors.transparent,
                    contentColor = PreviewLabTheme.colors.error,
                    borderColor = PreviewLabTheme.colors.error,
                    disabledContainerColor = PreviewLabTheme.colors.transparent,
                    disabledContentColor = PreviewLabTheme.colors.onDisabled,
                    disabledBorderColor = PreviewLabTheme.colors.disabled,
                ),
            shape = shape,
            elevation = null,
        )

    @Composable
    fun destructiveElevated(shape: Shape) =
        IconButtonStyle(
            colors =
                IconButtonColors(
                    containerColor = PreviewLabTheme.colors.error,
                    contentColor = PreviewLabTheme.colors.onError,
                    disabledContainerColor = PreviewLabTheme.colors.disabled,
                    disabledContentColor = PreviewLabTheme.colors.onDisabled,
                ),
            shape = shape,
            elevation = buttonElevation(),
        )

    @Composable
    fun destructiveGhost(shape: Shape) =
        IconButtonStyle(
            colors =
                IconButtonColors(
                    containerColor = PreviewLabTheme.colors.transparent,
                    contentColor = PreviewLabTheme.colors.error,
                    borderColor = PreviewLabTheme.colors.transparent,
                    disabledContainerColor = PreviewLabTheme.colors.transparent,
                    disabledContentColor = PreviewLabTheme.colors.onDisabled,
                    disabledBorderColor = PreviewLabTheme.colors.transparent,
                ),
            shape = shape,
            elevation = null,
        )

    @Composable
    fun ghost(shape: Shape) =
        IconButtonStyle(
            colors =
                IconButtonColors(
                    containerColor = PreviewLabTheme.colors.transparent,
                    contentColor = LocalContentColor.current,
                    disabledContainerColor = PreviewLabTheme.colors.transparent,
                    disabledContentColor = PreviewLabTheme.colors.onDisabled,
                ),
            shape = shape,
            elevation = null,
        )
}

@Immutable
data class IconButtonColors(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color? = null,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
    val disabledBorderColor: Color? = null,
) {
    @Composable
    fun containerColor(enabled: Boolean) = rememberUpdatedState(if (enabled) containerColor else disabledContainerColor)

    @Composable
    fun contentColor(enabled: Boolean) = rememberUpdatedState(if (enabled) contentColor else disabledContentColor)

    @Composable
    fun borderColor(enabled: Boolean) = rememberUpdatedState(if (enabled) borderColor else disabledBorderColor)
}

@Immutable
data class IconButtonStyle(
    val colors: IconButtonColors,
    val shape: Shape,
    val elevation: ButtonElevation? = null,
)

@Composable
@Preview
fun PrimaryIconButtonPreview() {
    PreviewLabTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BasicText(text = "Primary Icon Buttons", style = PreviewLabTheme.typography.h2)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(variant = IconButtonVariant.Primary) {
                    DummyIconForIconButtonPreview()
                }
                IconButton(variant = IconButtonVariant.PrimaryOutlined) {
                    DummyIconForIconButtonPreview()
                }
                IconButton(variant = IconButtonVariant.PrimaryElevated) {
                    DummyIconForIconButtonPreview()
                }
                IconButton(variant = IconButtonVariant.PrimaryGhost) {
                    DummyIconForIconButtonPreview()
                }
            }
        }
    }
}

@Composable
@Preview
fun SecondaryIconButtonPreview() {
    PreviewLabTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BasicText(text = "Secondary Icon Buttons", style = PreviewLabTheme.typography.h2)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(variant = IconButtonVariant.Secondary) {
                    DummyIconForIconButtonPreview()
                }
                IconButton(variant = IconButtonVariant.SecondaryOutlined) {
                    DummyIconForIconButtonPreview()
                }
                IconButton(variant = IconButtonVariant.SecondaryElevated) {
                    DummyIconForIconButtonPreview()
                }
                IconButton(variant = IconButtonVariant.SecondaryGhost) {
                    DummyIconForIconButtonPreview()
                }
            }
        }
    }
}

@Composable
@Preview
fun DestructiveIconButtonPreview() {
    PreviewLabTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BasicText(text = "Destructive Icon Buttons", style = PreviewLabTheme.typography.h2)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(variant = IconButtonVariant.Destructive) {
                    DummyIconForIconButtonPreview()
                }
                IconButton(variant = IconButtonVariant.DestructiveOutlined) {
                    DummyIconForIconButtonPreview()
                }
                IconButton(variant = IconButtonVariant.DestructiveElevated) {
                    DummyIconForIconButtonPreview()
                }
                IconButton(variant = IconButtonVariant.DestructiveGhost) {
                    DummyIconForIconButtonPreview()
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
@Preview
fun GhostIconButtonPreview() {
    PreviewLabTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BasicText(text = "Ghost Icon Buttons", style = PreviewLabTheme.typography.h2)

            FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8)).background(PreviewLabTheme.colors.background),
                    contentAlignment = Alignment.Center,
                ) {
                    CompositionLocalProvider(LocalContentColor provides contentColorFor(color = PreviewLabTheme.colors.background)) {
                        IconButton(variant = IconButtonVariant.Ghost) {
                            DummyIconForIconButtonPreview()
                        }
                    }
                }

                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8)).background(PreviewLabTheme.colors.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    CompositionLocalProvider(LocalContentColor provides contentColorFor(color = PreviewLabTheme.colors.primary)) {
                        IconButton(variant = IconButtonVariant.Ghost) {
                            DummyIconForIconButtonPreview()
                        }
                    }
                }

                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8)).background(PreviewLabTheme.colors.secondary),
                    contentAlignment = Alignment.Center,
                ) {
                    CompositionLocalProvider(LocalContentColor provides contentColorFor(color = PreviewLabTheme.colors.secondary)) {
                        IconButton(variant = IconButtonVariant.Ghost) {
                            DummyIconForIconButtonPreview()
                        }
                    }
                }

                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8)).background(PreviewLabTheme.colors.tertiary),
                    contentAlignment = Alignment.Center,
                ) {
                    CompositionLocalProvider(LocalContentColor provides contentColorFor(color = PreviewLabTheme.colors.tertiary)) {
                        IconButton(variant = IconButtonVariant.Ghost) {
                            DummyIconForIconButtonPreview()
                        }
                    }
                }

                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8)).background(PreviewLabTheme.colors.surface),
                    contentAlignment = Alignment.Center,
                ) {
                    CompositionLocalProvider(LocalContentColor provides contentColorFor(color = PreviewLabTheme.colors.surface)) {
                        IconButton(variant = IconButtonVariant.Ghost) {
                            DummyIconForIconButtonPreview()
                        }
                    }
                }

                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8)).background(PreviewLabTheme.colors.error),
                    contentAlignment = Alignment.Center,
                ) {
                    CompositionLocalProvider(LocalContentColor provides contentColorFor(color = PreviewLabTheme.colors.error)) {
                        IconButton(variant = IconButtonVariant.Ghost) {
                            DummyIconForIconButtonPreview()
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun IconButtonShapesPreview() {
    PreviewLabTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BasicText(text = "Square Shape", style = PreviewLabTheme.typography.h2)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(
                    variant = IconButtonVariant.Primary,
                    shape = IconButtonDefaults.ButtonSquareShape,
                ) {
                    DummyIconForIconButtonPreview()
                }
                IconButton(
                    variant = IconButtonVariant.PrimaryOutlined,
                    shape = IconButtonDefaults.ButtonSquareShape,
                ) {
                    DummyIconForIconButtonPreview()
                }
            }

            BasicText(text = "Circle Shape", style = PreviewLabTheme.typography.h2)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(
                    variant = IconButtonVariant.Primary,
                    shape = IconButtonDefaults.ButtonCircleShape,
                ) {
                    DummyIconForIconButtonPreview()
                }
                IconButton(
                    variant = IconButtonVariant.PrimaryOutlined,
                    shape = IconButtonDefaults.ButtonCircleShape,
                ) {
                    DummyIconForIconButtonPreview()
                }
            }
        }
    }
}

@Composable
@Preview
private fun DummyIconForIconButtonPreview() {
    Canvas(modifier = Modifier.size(16.dp)) {
        val center = size / 2f
        val radius = size.minDimension * 0.4f
        val strokeWidth = 4f
        val cap = StrokeCap.Round

        drawLine(
            color = Color.Black,
            start = Offset(center.width - radius, center.height),
            end = Offset(center.width + radius, center.height),
            strokeWidth = strokeWidth,
            cap = cap,
        )

        drawLine(
            color = Color.Black,
            start = Offset(center.width, center.height - radius),
            end = Offset(center.width, center.height + radius),
            strokeWidth = strokeWidth,
            cap = cap,
        )

        val diagonalRadius = radius * 0.75f
        drawLine(
            color = Color.Black,
            start =
                Offset(
                    center.width - diagonalRadius,
                    center.height - diagonalRadius,
                ),
            end =
                Offset(
                    center.width + diagonalRadius,
                    center.height + diagonalRadius,
                ),
            strokeWidth = strokeWidth,
            cap = cap,
        )

        drawLine(
            color = Color.Black,
            start =
                Offset(
                    center.width - diagonalRadius,
                    center.height + diagonalRadius,
                ),
            end =
                Offset(
                    center.width + diagonalRadius,
                    center.height - diagonalRadius,
                ),
            strokeWidth = strokeWidth,
            cap = cap,
        )
    }
}
