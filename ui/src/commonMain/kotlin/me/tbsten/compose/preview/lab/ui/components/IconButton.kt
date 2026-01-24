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
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.LocalContentColor
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.contentColorFor
import me.tbsten.compose.preview.lab.ui.foundation.ButtonElevation
import androidx.compose.ui.tooling.preview.Preview

@Composable
@InternalComposePreviewLabApi
fun PreviewLabIconButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    variant: PreviewLabIconButtonVariant = PreviewLabIconButtonVariant.Primary,
    shape: Shape = PreviewLabIconButtonDefaults.ButtonSquareShape,
    onClick: () -> Unit = {},
    contentPadding: PaddingValues = PreviewLabIconButtonDefaults.ButtonPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit,
) {
    val style = PreviewLabIconButtonDefaults.styleFor(variant, shape)

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
    style: PreviewLabIconButtonStyle,
    onClick: () -> Unit,
    contentPadding: PaddingValues,
    interactionSource: MutableInteractionSource,
    content: @Composable () -> Unit,
) {
    val containerColor = style.colors.containerColor(enabled).value
    val contentColor = style.colors.contentColor(enabled).value
    val borderColor = style.colors.borderColor(enabled).value
    val borderStroke = if (borderColor != null) BorderStroke(PreviewLabIconButtonDefaults.OutlineHeight, borderColor) else null

    val shadowElevation = style.elevation?.shadowElevation(enabled, interactionSource)?.value ?: 0.dp

    PreviewLabSurface(
        onClick = onClick,
        modifier =
        modifier.defaultMinSize(
            minWidth = PreviewLabIconButtonDefaults.ButtonSize,
            minHeight = PreviewLabIconButtonDefaults.ButtonSize,
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

@InternalComposePreviewLabApi
enum class PreviewLabIconButtonVariant {
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

@InternalComposePreviewLabApi
object PreviewLabIconButtonDefaults {
    val ButtonSize = 44.dp
    val ButtonPadding = PaddingValues(4.dp)
    val ButtonSquareShape = RoundedCornerShape(12.dp)
    val ButtonCircleShape = RoundedCornerShape(percent = 50)
    val OutlineHeight = 1.dp

    @Composable
    fun buttonElevation() = ButtonElevation(
        defaultElevation = 2.dp,
        pressedElevation = 2.dp,
        focusedElevation = 2.dp,
        hoveredElevation = 2.dp,
        disabledElevation = 0.dp,
    )

    @Composable
    fun styleFor(variant: PreviewLabIconButtonVariant, shape: Shape): PreviewLabIconButtonStyle = when (variant) {
        PreviewLabIconButtonVariant.Primary -> primaryFilled(shape)
        PreviewLabIconButtonVariant.PrimaryOutlined -> primaryOutlined(shape)
        PreviewLabIconButtonVariant.PrimaryElevated -> primaryElevated(shape)
        PreviewLabIconButtonVariant.PrimaryGhost -> primaryGhost(shape)
        PreviewLabIconButtonVariant.Secondary -> secondaryFilled(shape)
        PreviewLabIconButtonVariant.SecondaryOutlined -> secondaryOutlined(shape)
        PreviewLabIconButtonVariant.SecondaryElevated -> secondaryElevated(shape)
        PreviewLabIconButtonVariant.SecondaryGhost -> secondaryGhost(shape)
        PreviewLabIconButtonVariant.Destructive -> destructiveFilled(shape)
        PreviewLabIconButtonVariant.DestructiveOutlined -> destructiveOutlined(shape)
        PreviewLabIconButtonVariant.DestructiveElevated -> destructiveElevated(shape)
        PreviewLabIconButtonVariant.DestructiveGhost -> destructiveGhost(shape)
        PreviewLabIconButtonVariant.Ghost -> ghost(shape)
    }

    @Composable
    fun primaryFilled(shape: Shape) = PreviewLabIconButtonStyle(
        colors =
        PreviewLabIconButtonColors(
            containerColor = PreviewLabTheme.colors.primary,
            contentColor = PreviewLabTheme.colors.onPrimary,
            disabledContainerColor = PreviewLabTheme.colors.disabled,
            disabledContentColor = PreviewLabTheme.colors.onDisabled,
        ),
        shape = shape,
        elevation = null,
    )

    @Composable
    fun primaryOutlined(shape: Shape) = PreviewLabIconButtonStyle(
        colors =
        PreviewLabIconButtonColors(
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
    fun primaryElevated(shape: Shape) = PreviewLabIconButtonStyle(
        colors =
        PreviewLabIconButtonColors(
            containerColor = PreviewLabTheme.colors.primary,
            contentColor = PreviewLabTheme.colors.onPrimary,
            disabledContainerColor = PreviewLabTheme.colors.disabled,
            disabledContentColor = PreviewLabTheme.colors.onDisabled,
        ),
        shape = shape,
        elevation = buttonElevation(),
    )

    @Composable
    fun primaryGhost(shape: Shape) = PreviewLabIconButtonStyle(
        colors =
        PreviewLabIconButtonColors(
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
    fun secondaryFilled(shape: Shape) = PreviewLabIconButtonStyle(
        colors =
        PreviewLabIconButtonColors(
            containerColor = PreviewLabTheme.colors.secondary,
            contentColor = PreviewLabTheme.colors.onSecondary,
            disabledContainerColor = PreviewLabTheme.colors.disabled,
            disabledContentColor = PreviewLabTheme.colors.onDisabled,
        ),
        shape = shape,
        elevation = null,
    )

    @Composable
    fun secondaryOutlined(shape: Shape) = PreviewLabIconButtonStyle(
        colors =
        PreviewLabIconButtonColors(
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
    fun secondaryElevated(shape: Shape) = PreviewLabIconButtonStyle(
        colors =
        PreviewLabIconButtonColors(
            containerColor = PreviewLabTheme.colors.secondary,
            contentColor = PreviewLabTheme.colors.onSecondary,
            disabledContainerColor = PreviewLabTheme.colors.disabled,
            disabledContentColor = PreviewLabTheme.colors.onDisabled,
        ),
        shape = shape,
        elevation = buttonElevation(),
    )

    @Composable
    fun secondaryGhost(shape: Shape) = PreviewLabIconButtonStyle(
        colors =
        PreviewLabIconButtonColors(
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
    fun destructiveFilled(shape: Shape) = PreviewLabIconButtonStyle(
        colors =
        PreviewLabIconButtonColors(
            containerColor = PreviewLabTheme.colors.error,
            contentColor = PreviewLabTheme.colors.onError,
            disabledContainerColor = PreviewLabTheme.colors.disabled,
            disabledContentColor = PreviewLabTheme.colors.onDisabled,
        ),
        shape = shape,
        elevation = null,
    )

    @Composable
    fun destructiveOutlined(shape: Shape) = PreviewLabIconButtonStyle(
        colors =
        PreviewLabIconButtonColors(
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
    fun destructiveElevated(shape: Shape) = PreviewLabIconButtonStyle(
        colors =
        PreviewLabIconButtonColors(
            containerColor = PreviewLabTheme.colors.error,
            contentColor = PreviewLabTheme.colors.onError,
            disabledContainerColor = PreviewLabTheme.colors.disabled,
            disabledContentColor = PreviewLabTheme.colors.onDisabled,
        ),
        shape = shape,
        elevation = buttonElevation(),
    )

    @Composable
    fun destructiveGhost(shape: Shape) = PreviewLabIconButtonStyle(
        colors =
        PreviewLabIconButtonColors(
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
    fun ghost(shape: Shape) = PreviewLabIconButtonStyle(
        colors =
        PreviewLabIconButtonColors(
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
@InternalComposePreviewLabApi
data class PreviewLabIconButtonColors(
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
@InternalComposePreviewLabApi
data class PreviewLabIconButtonStyle(val colors: PreviewLabIconButtonColors, val shape: Shape, val elevation: ButtonElevation? = null)

@Composable
@Preview
private fun PrimaryIconButtonPreview() {
    PreviewLabTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BasicText(text = "Primary Icon Buttons", style = PreviewLabTheme.typography.h2)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PreviewLabIconButton(variant = PreviewLabIconButtonVariant.Primary) {
                    DummyIconForIconButtonPreview()
                }
                PreviewLabIconButton(variant = PreviewLabIconButtonVariant.PrimaryOutlined) {
                    DummyIconForIconButtonPreview()
                }
                PreviewLabIconButton(variant = PreviewLabIconButtonVariant.PrimaryElevated) {
                    DummyIconForIconButtonPreview()
                }
                PreviewLabIconButton(variant = PreviewLabIconButtonVariant.PrimaryGhost) {
                    DummyIconForIconButtonPreview()
                }
            }
        }
    }
}

@Composable
@Preview
private fun SecondaryIconButtonPreview() {
    PreviewLabTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BasicText(text = "Secondary Icon Buttons", style = PreviewLabTheme.typography.h2)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PreviewLabIconButton(variant = PreviewLabIconButtonVariant.Secondary) {
                    DummyIconForIconButtonPreview()
                }
                PreviewLabIconButton(variant = PreviewLabIconButtonVariant.SecondaryOutlined) {
                    DummyIconForIconButtonPreview()
                }
                PreviewLabIconButton(variant = PreviewLabIconButtonVariant.SecondaryElevated) {
                    DummyIconForIconButtonPreview()
                }
                PreviewLabIconButton(variant = PreviewLabIconButtonVariant.SecondaryGhost) {
                    DummyIconForIconButtonPreview()
                }
            }
        }
    }
}

@Composable
@Preview
private fun DestructiveIconButtonPreview() {
    PreviewLabTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BasicText(text = "Destructive Icon Buttons", style = PreviewLabTheme.typography.h2)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PreviewLabIconButton(variant = PreviewLabIconButtonVariant.Destructive) {
                    DummyIconForIconButtonPreview()
                }
                PreviewLabIconButton(variant = PreviewLabIconButtonVariant.DestructiveOutlined) {
                    DummyIconForIconButtonPreview()
                }
                PreviewLabIconButton(variant = PreviewLabIconButtonVariant.DestructiveElevated) {
                    DummyIconForIconButtonPreview()
                }
                PreviewLabIconButton(variant = PreviewLabIconButtonVariant.DestructiveGhost) {
                    DummyIconForIconButtonPreview()
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
@Preview
private fun GhostIconButtonPreview() {
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
                    CompositionLocalProvider(
                        LocalContentColor provides contentColorFor(color = PreviewLabTheme.colors.background),
                    ) {
                        PreviewLabIconButton(variant = PreviewLabIconButtonVariant.Ghost) {
                            DummyIconForIconButtonPreview()
                        }
                    }
                }

                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8)).background(PreviewLabTheme.colors.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides contentColorFor(color = PreviewLabTheme.colors.primary),
                    ) {
                        PreviewLabIconButton(variant = PreviewLabIconButtonVariant.Ghost) {
                            DummyIconForIconButtonPreview()
                        }
                    }
                }

                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8)).background(PreviewLabTheme.colors.secondary),
                    contentAlignment = Alignment.Center,
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides contentColorFor(color = PreviewLabTheme.colors.secondary),
                    ) {
                        PreviewLabIconButton(variant = PreviewLabIconButtonVariant.Ghost) {
                            DummyIconForIconButtonPreview()
                        }
                    }
                }

                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8)).background(PreviewLabTheme.colors.tertiary),
                    contentAlignment = Alignment.Center,
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides contentColorFor(color = PreviewLabTheme.colors.tertiary),
                    ) {
                        PreviewLabIconButton(variant = PreviewLabIconButtonVariant.Ghost) {
                            DummyIconForIconButtonPreview()
                        }
                    }
                }

                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8)).background(PreviewLabTheme.colors.surface),
                    contentAlignment = Alignment.Center,
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides contentColorFor(color = PreviewLabTheme.colors.surface),
                    ) {
                        PreviewLabIconButton(variant = PreviewLabIconButtonVariant.Ghost) {
                            DummyIconForIconButtonPreview()
                        }
                    }
                }

                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8)).background(PreviewLabTheme.colors.error),
                    contentAlignment = Alignment.Center,
                ) {
                    CompositionLocalProvider(LocalContentColor provides contentColorFor(color = PreviewLabTheme.colors.error)) {
                        PreviewLabIconButton(variant = PreviewLabIconButtonVariant.Ghost) {
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
private fun IconButtonShapesPreview() {
    PreviewLabTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BasicText(text = "Square Shape", style = PreviewLabTheme.typography.h2)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PreviewLabIconButton(
                    variant = PreviewLabIconButtonVariant.Primary,
                    shape = PreviewLabIconButtonDefaults.ButtonSquareShape,
                ) {
                    DummyIconForIconButtonPreview()
                }
                PreviewLabIconButton(
                    variant = PreviewLabIconButtonVariant.PrimaryOutlined,
                    shape = PreviewLabIconButtonDefaults.ButtonSquareShape,
                ) {
                    DummyIconForIconButtonPreview()
                }
            }

            BasicText(text = "Circle Shape", style = PreviewLabTheme.typography.h2)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PreviewLabIconButton(
                    variant = PreviewLabIconButtonVariant.Primary,
                    shape = PreviewLabIconButtonDefaults.ButtonCircleShape,
                ) {
                    DummyIconForIconButtonPreview()
                }
                PreviewLabIconButton(
                    variant = PreviewLabIconButtonVariant.PrimaryOutlined,
                    shape = PreviewLabIconButtonDefaults.ButtonCircleShape,
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
