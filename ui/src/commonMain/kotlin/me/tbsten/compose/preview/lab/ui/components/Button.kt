package me.tbsten.compose.preview.lab.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.LocalContentColor
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.foundation.ButtonElevation

@Composable
@InternalComposePreviewLabApi
fun PreviewLabButton(
    modifier: Modifier = Modifier,
    text: String? = null,
    isEnabled: Boolean = true,
    isSelected: Boolean = false,
    isLoading: Boolean = false,
    variant: PreviewLabButtonVariant = PreviewLabButtonVariant.Primary,
    shape: Shape? = null,
    onClick: () -> Unit = {},
    contentPadding: PaddingValues = PreviewLabButtonDefaults.contentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: (@Composable () -> Unit)? = null,
) {
    val style = previewLabButtonStyleFor(variant, isSelected = isSelected)
    ButtonComponent(
        text = text,
        modifier = modifier,
        enabled = isEnabled,
        loading = isLoading,
        style = style,
        shape = shape ?: style.shape,
        onClick = onClick,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content,
    )
}

@Composable
private fun ButtonComponent(
    text: String? = null,
    modifier: Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    style: PreviewLabButtonStyle,
    shape: Shape = style.shape,
    onClick: () -> Unit,
    contentPadding: PaddingValues = PreviewLabButtonDefaults.contentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: (@Composable () -> Unit)? = null,
) {
    val containerColor = style.colors.containerColor(enabled).value
    val contentColor = style.colors.contentColor(enabled).value
    val borderColor = style.colors.borderColor(enabled).value
    val borderStroke =
        if (borderColor != null) {
            BorderStroke(
                PreviewLabButtonDefaults.OutlineHeight,
                borderColor,
            )
        } else {
            null
        }

    val shadowElevation = style.elevation?.shadowElevation(enabled, interactionSource)?.value ?: 0.dp

//    in case of full width button
//    val buttonModifier = modifier.fillMaxWidth()

    PreviewLabSurface(
        onClick = onClick,
        modifier =
        modifier
            .semantics { role = Role.Button },
        enabled = enabled,
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        border = borderStroke,
        shadowElevation = shadowElevation,
        interactionSource = interactionSource,
    ) {
        DefaultButtonContent(
            text = text,
            loading = loading,
            contentColor = contentColor,
            content = content,
            modifier = Modifier.padding(contentPadding),
        )
    }
}

@Composable
private fun DefaultButtonContent(
    modifier: Modifier = Modifier,
    text: String? = null,
    loading: Boolean,
    contentColor: Color,
    content: (@Composable () -> Unit)? = null,
) {
    if (text?.isEmpty() == false) {
        Row(
            modifier,
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
//            if (!loading) {
//                CircularProgressIndicator(
//                    color = contentColor,
//                    modifier = Modifier.size(20.dp),
//                    strokeWidth = 2.dp
//                )
//            }

            PreviewLabText(
                text = AnnotatedString(text = text),
                textAlign = TextAlign.Center,
                style = PreviewLabTheme.typography.button,
                overflow = TextOverflow.Clip,
                color = contentColor,
            )
        }
    } else if (content != null) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            content()
        }
    }
}

@InternalComposePreviewLabApi
enum class PreviewLabButtonVariant {
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

@Composable
@InternalComposePreviewLabApi
fun previewLabButtonStyleFor(variant: PreviewLabButtonVariant, isSelected: Boolean): PreviewLabButtonStyle = when (variant) {
    PreviewLabButtonVariant.Primary -> PreviewLabButtonDefaults.primaryFilled(isSelected = isSelected)
    PreviewLabButtonVariant.PrimaryOutlined -> PreviewLabButtonDefaults.primaryOutlined(isSelected = isSelected)
    PreviewLabButtonVariant.PrimaryElevated -> PreviewLabButtonDefaults.primaryElevated(isSelected = isSelected)
    PreviewLabButtonVariant.PrimaryGhost -> PreviewLabButtonDefaults.primaryGhost(isSelected = isSelected)
    PreviewLabButtonVariant.Secondary -> PreviewLabButtonDefaults.secondaryFilled(isSelected = isSelected)
    PreviewLabButtonVariant.SecondaryOutlined -> PreviewLabButtonDefaults.secondaryOutlined(isSelected = isSelected)
    PreviewLabButtonVariant.SecondaryElevated -> PreviewLabButtonDefaults.secondaryElevated(isSelected = isSelected)
    PreviewLabButtonVariant.SecondaryGhost -> PreviewLabButtonDefaults.secondaryGhost(isSelected = isSelected)
    PreviewLabButtonVariant.Destructive -> PreviewLabButtonDefaults.destructiveFilled(isSelected = isSelected)
    PreviewLabButtonVariant.DestructiveOutlined -> PreviewLabButtonDefaults.destructiveOutlined(isSelected = isSelected)
    PreviewLabButtonVariant.DestructiveElevated -> PreviewLabButtonDefaults.destructiveElevated(isSelected = isSelected)
    PreviewLabButtonVariant.DestructiveGhost -> PreviewLabButtonDefaults.destructiveGhost(isSelected = isSelected)
    PreviewLabButtonVariant.Ghost -> PreviewLabButtonDefaults.ghost(isSelected = isSelected)
}

@InternalComposePreviewLabApi
object PreviewLabButtonDefaults {
    @InternalComposePreviewLabApi
    val MinHeight = 44.dp

    @InternalComposePreviewLabApi
    val OutlineHeight = 1.dp
    val ButtonHorizontalPadding = 16.dp
    val ButtonVerticalPadding = 8.dp
    val ButtonShape = RoundedCornerShape(12)

    val contentPadding =
        PaddingValues(
            start = ButtonHorizontalPadding,
            top = ButtonVerticalPadding,
            end = ButtonHorizontalPadding,
            bottom = ButtonVerticalPadding,
        )

    val filledShape = ButtonShape
    val elevatedShape = ButtonShape
    val outlinedShape = ButtonShape

    @Composable
    fun buttonElevation() = ButtonElevation(
        defaultElevation = 2.dp,
        pressedElevation = 2.dp,
        focusedElevation = 2.dp,
        hoveredElevation = 2.dp,
        disabledElevation = 0.dp,
    )

    @Composable
    fun primaryFilled(isSelected: Boolean) = PreviewLabButtonStyle(
        colors =
        PreviewLabButtonColors(
            containerColor = PreviewLabTheme.colors.primary,
            contentColor = PreviewLabTheme.colors.onPrimary,
            disabledContainerColor = PreviewLabTheme.colors.disabled,
            disabledContentColor = PreviewLabTheme.colors.onDisabled,
            borderColor = if (isSelected) PreviewLabTheme.colors.primary else Color.Transparent,
        ),
        shape = filledShape,
        elevation = null,
        contentPadding = contentPadding,
    )

    @Composable
    fun primaryElevated(isSelected: Boolean) = PreviewLabButtonStyle(
        colors =
        PreviewLabButtonColors(
            containerColor = PreviewLabTheme.colors.primary,
            contentColor = PreviewLabTheme.colors.onPrimary,
            disabledContainerColor = PreviewLabTheme.colors.disabled,
            disabledContentColor = PreviewLabTheme.colors.onDisabled,
            borderColor = if (isSelected) PreviewLabTheme.colors.primary else Color.Transparent,
        ),
        shape = elevatedShape,
        elevation = buttonElevation(),
        contentPadding = contentPadding,
    )

    @Composable
    fun primaryOutlined(isSelected: Boolean) = PreviewLabButtonStyle(
        colors =
        PreviewLabButtonColors(
            containerColor = if (isSelected) PreviewLabTheme.colors.primary else PreviewLabTheme.colors.transparent,
            contentColor = PreviewLabTheme.colors.primary,
            borderColor = PreviewLabTheme.colors.primary,
            disabledContainerColor = PreviewLabTheme.colors.transparent,
            disabledContentColor = PreviewLabTheme.colors.onDisabled,
            disabledBorderColor = PreviewLabTheme.colors.disabled,
        ),
        shape = outlinedShape,
        elevation = null,
        contentPadding = contentPadding,
    )

    @Composable
    fun primaryGhost(isSelected: Boolean) = PreviewLabButtonStyle(
        colors =
        PreviewLabButtonColors(
            containerColor = if (isSelected) {
                PreviewLabTheme.colors.primary
                    .copy(alpha = 0.25f)
            } else {
                PreviewLabTheme.colors.transparent
            },
            contentColor = PreviewLabTheme.colors.primary,
            borderColor = PreviewLabTheme.colors.transparent,
            disabledContainerColor = PreviewLabTheme.colors.transparent,
            disabledContentColor = PreviewLabTheme.colors.onDisabled,
            disabledBorderColor = PreviewLabTheme.colors.transparent,
        ),
        shape = filledShape,
        elevation = null,
        contentPadding = contentPadding,
    )

    @Composable
    fun secondaryFilled(isSelected: Boolean) = PreviewLabButtonStyle(
        colors =
        PreviewLabButtonColors(
            containerColor = PreviewLabTheme.colors.secondary,
            contentColor = PreviewLabTheme.colors.onSecondary,
            disabledContainerColor = PreviewLabTheme.colors.disabled,
            disabledContentColor = PreviewLabTheme.colors.onDisabled,
            borderColor = if (isSelected) PreviewLabTheme.colors.primary else Color.Transparent,
        ),
        shape = filledShape,
        elevation = null,
        contentPadding = contentPadding,
    )

    @Composable
    fun secondaryElevated(isSelected: Boolean) = PreviewLabButtonStyle(
        colors =
        PreviewLabButtonColors(
            containerColor = PreviewLabTheme.colors.secondary,
            contentColor = PreviewLabTheme.colors.onSecondary,
            disabledContainerColor = PreviewLabTheme.colors.disabled,
            disabledContentColor = PreviewLabTheme.colors.onDisabled,
            borderColor = if (isSelected) PreviewLabTheme.colors.primary else Color.Transparent,
        ),
        shape = elevatedShape,
        elevation = buttonElevation(),
        contentPadding = contentPadding,
    )

    @Composable
    fun secondaryOutlined(isSelected: Boolean) = PreviewLabButtonStyle(
        colors =
        PreviewLabButtonColors(
            containerColor = if (isSelected) PreviewLabTheme.colors.primary else PreviewLabTheme.colors.transparent,
            contentColor = PreviewLabTheme.colors.secondary,
            borderColor = PreviewLabTheme.colors.secondary,
            disabledContainerColor = PreviewLabTheme.colors.transparent,
            disabledContentColor = PreviewLabTheme.colors.onDisabled,
            disabledBorderColor = PreviewLabTheme.colors.disabled,
        ),
        shape = outlinedShape,
        elevation = null,
        contentPadding = contentPadding,
    )

    @Composable
    fun secondaryGhost(isSelected: Boolean) = PreviewLabButtonStyle(
        colors =
        PreviewLabButtonColors(
            containerColor = if (isSelected) {
                PreviewLabTheme.colors.secondary
                    .copy(alpha = 0.25f)
            } else {
                PreviewLabTheme.colors.transparent
            },
            contentColor = PreviewLabTheme.colors.secondary,
            borderColor = PreviewLabTheme.colors.transparent,
            disabledContainerColor = PreviewLabTheme.colors.transparent,
            disabledContentColor = PreviewLabTheme.colors.onDisabled,
            disabledBorderColor = PreviewLabTheme.colors.transparent,
        ),
        shape = filledShape,
        elevation = null,
        contentPadding = contentPadding,
    )

    @Composable
    fun destructiveFilled(isSelected: Boolean) = PreviewLabButtonStyle(
        colors =
        PreviewLabButtonColors(
            containerColor = PreviewLabTheme.colors.error,
            contentColor = PreviewLabTheme.colors.onError,
            disabledContainerColor = PreviewLabTheme.colors.disabled,
            disabledContentColor = PreviewLabTheme.colors.onDisabled,
            borderColor = if (isSelected) PreviewLabTheme.colors.error.copy(alpha = 0.5f) else Color.Transparent,
        ),
        shape = filledShape,
        elevation = null,
        contentPadding = contentPadding,
    )

    @Composable
    fun destructiveElevated(isSelected: Boolean) = PreviewLabButtonStyle(
        colors =
        PreviewLabButtonColors(
            containerColor = PreviewLabTheme.colors.error,
            contentColor = PreviewLabTheme.colors.onError,
            disabledContainerColor = PreviewLabTheme.colors.disabled,
            disabledContentColor = PreviewLabTheme.colors.onDisabled,
            borderColor = if (isSelected) PreviewLabTheme.colors.error else Color.Transparent,
        ),
        shape = elevatedShape,
        elevation = buttonElevation(),
        contentPadding = contentPadding,
    )

    @Composable
    fun destructiveOutlined(isSelected: Boolean) = PreviewLabButtonStyle(
        colors =
        PreviewLabButtonColors(
            containerColor = if (isSelected) PreviewLabTheme.colors.error else PreviewLabTheme.colors.transparent,
            contentColor = PreviewLabTheme.colors.error,
            borderColor = PreviewLabTheme.colors.error,
            disabledContainerColor = PreviewLabTheme.colors.transparent,
            disabledContentColor = PreviewLabTheme.colors.onDisabled,
            disabledBorderColor = PreviewLabTheme.colors.disabled,
        ),
        shape = outlinedShape,
        elevation = null,
        contentPadding = contentPadding,
    )

    @Composable
    fun destructiveGhost(isSelected: Boolean) = PreviewLabButtonStyle(
        colors =
        PreviewLabButtonColors(
            containerColor = if (isSelected) {
                PreviewLabTheme.colors.error
                    .copy(alpha = 0.25f)
            } else {
                PreviewLabTheme.colors.transparent
            },
            contentColor = PreviewLabTheme.colors.error,
            borderColor = PreviewLabTheme.colors.transparent,
            disabledContainerColor = PreviewLabTheme.colors.transparent,
            disabledContentColor = PreviewLabTheme.colors.onDisabled,
            disabledBorderColor = PreviewLabTheme.colors.transparent,
        ),
        shape = filledShape,
        elevation = null,
        contentPadding = contentPadding,
    )

    @Composable
    fun ghost(isSelected: Boolean) = PreviewLabButtonStyle(
        colors =
        PreviewLabButtonColors(
            containerColor = if (isSelected) {
                PreviewLabTheme.colors.black
                    .copy(alpha = 0.5f)
            } else {
                PreviewLabTheme.colors.transparent
            },
            contentColor = LocalContentColor.current,
            borderColor = PreviewLabTheme.colors.transparent,
            disabledContainerColor = PreviewLabTheme.colors.transparent,
            disabledContentColor = PreviewLabTheme.colors.onDisabled,
            disabledBorderColor = PreviewLabTheme.colors.transparent,
        ),
        shape = filledShape,
        elevation = null,
        contentPadding = contentPadding,
    )
}

@Immutable
@InternalComposePreviewLabApi
data class PreviewLabButtonColors(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color? = null,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
    val disabledBorderColor: Color? = null,
) {
    @Composable
    fun containerColor(enabled: Boolean) =
        rememberUpdatedState(newValue = if (enabled) containerColor else disabledContainerColor)

    @Composable
    fun contentColor(enabled: Boolean) = rememberUpdatedState(newValue = if (enabled) contentColor else disabledContentColor)

    @Composable
    fun borderColor(enabled: Boolean) = rememberUpdatedState(newValue = if (enabled) borderColor else disabledBorderColor)
}

@Immutable
@InternalComposePreviewLabApi
data class PreviewLabButtonStyle(
    val colors: PreviewLabButtonColors,
    val shape: Shape,
    val elevation: ButtonElevation? = null,
    val contentPadding: PaddingValues,
)

@Composable
@Preview
private fun ButtonPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PrimaryButtonPreview()
        SecondaryButtonPreview()
        DestructiveButtonPreview()
    }
}

@Composable
@Preview
private fun PrimaryButtonPreview() {
    PreviewLabTheme {
        Column(
            modifier =
            Modifier
                .background(PreviewLabTheme.colors.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PreviewLabText(text = "Primary Buttons", style = PreviewLabTheme.typography.h2)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PreviewLabButton(text = "PrimaryFilled", variant = PreviewLabButtonVariant.Primary, onClick = {})

                PreviewLabButton(
                    text = "Disabled",
                    variant = PreviewLabButtonVariant.Primary,
                    isEnabled = false,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PreviewLabButton(text = "PrimaryOutlined", variant = PreviewLabButtonVariant.PrimaryOutlined, onClick = {})

                PreviewLabButton(
                    text = "Disabled",
                    variant = PreviewLabButtonVariant.PrimaryOutlined,
                    isEnabled = false,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PreviewLabButton(text = "PrimaryElevated", variant = PreviewLabButtonVariant.PrimaryElevated, onClick = {})

                PreviewLabButton(
                    text = "Disabled",
                    variant = PreviewLabButtonVariant.PrimaryElevated,
                    isEnabled = false,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PreviewLabButton(text = "PrimaryGhost", variant = PreviewLabButtonVariant.PrimaryGhost, onClick = {})

                PreviewLabButton(
                    text = "Disabled",
                    variant = PreviewLabButtonVariant.PrimaryGhost,
                    isEnabled = false,
                )
            }
        }
    }
}

@Composable
@Preview
private fun SecondaryButtonPreview() {
    PreviewLabTheme {
        Column(
            modifier =
            Modifier
                .background(PreviewLabTheme.colors.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PreviewLabText(text = "Secondary Buttons", style = PreviewLabTheme.typography.h2)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PreviewLabButton(text = "SecondaryFilled", variant = PreviewLabButtonVariant.Secondary, onClick = {})

                PreviewLabButton(
                    text = "Disabled",
                    variant = PreviewLabButtonVariant.Secondary,
                    isEnabled = false,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PreviewLabButton(text = "SecondaryOutlined", variant = PreviewLabButtonVariant.SecondaryOutlined, onClick = {})

                PreviewLabButton(
                    text = "Disabled",
                    variant = PreviewLabButtonVariant.SecondaryOutlined,
                    isEnabled = false,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PreviewLabButton(text = "SecondaryElevated", variant = PreviewLabButtonVariant.SecondaryElevated, onClick = {})

                PreviewLabButton(
                    text = "Disabled",
                    variant = PreviewLabButtonVariant.SecondaryElevated,
                    isEnabled = false,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PreviewLabButton(text = "SecondaryGhost", variant = PreviewLabButtonVariant.SecondaryGhost, onClick = {})

                PreviewLabButton(
                    text = "Disabled",
                    variant = PreviewLabButtonVariant.SecondaryGhost,
                    isEnabled = false,
                )
            }
        }
    }
}

@Composable
@Preview
private fun DestructiveButtonPreview() {
    PreviewLabTheme {
        Column(
            modifier =
            Modifier
                .background(PreviewLabTheme.colors.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PreviewLabText(text = "Destructive Buttons", style = PreviewLabTheme.typography.h2)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PreviewLabButton(text = "DestructiveFilled", variant = PreviewLabButtonVariant.Destructive, onClick = {})

                PreviewLabButton(
                    text = "Disabled",
                    variant = PreviewLabButtonVariant.Destructive,
                    isEnabled = false,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PreviewLabButton(
                    text = "DestructiveOutlined",
                    variant = PreviewLabButtonVariant.DestructiveOutlined,
                    onClick = {
                    },
                )

                PreviewLabButton(
                    text = "Disabled",
                    variant = PreviewLabButtonVariant.DestructiveOutlined,
                    isEnabled = false,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PreviewLabButton(
                    text = "DestructiveElevated",
                    variant = PreviewLabButtonVariant.DestructiveElevated,
                    onClick = {
                    },
                )

                PreviewLabButton(
                    text = "Disabled",
                    variant = PreviewLabButtonVariant.DestructiveElevated,
                    isEnabled = false,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PreviewLabButton(text = "DestructiveGhost", variant = PreviewLabButtonVariant.DestructiveGhost, onClick = {})

                PreviewLabButton(
                    text = "Disabled",
                    variant = PreviewLabButtonVariant.DestructiveGhost,
                    isEnabled = false,
                )
            }
        }
    }
}
