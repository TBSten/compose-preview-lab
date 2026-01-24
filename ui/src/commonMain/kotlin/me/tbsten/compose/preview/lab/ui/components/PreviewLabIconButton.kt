package me.tbsten.compose.preview.lab.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.LocalContentColor

@Composable
@InternalComposePreviewLabApi
fun PreviewLabIconButton(
    imageVector: ImageVector,
    contentDescription: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: PreviewLabIconButtonVariant = PreviewLabIconButtonVariant.Ghost,
    tint: Color? = null,
) = if (contentDescription == null) {
    IconButtonContent(
        imageVector = imageVector,
        contentDescription = contentDescription,
        onClick = onClick,
        enabled = enabled,
        variant = variant,
        tint = tint,
        modifier = modifier,
    )
} else {
    PreviewLabTooltipBox(
        tooltip = contentDescription,
    ) {
        IconButtonContent(
            imageVector = imageVector,
            contentDescription = contentDescription,
            onClick = onClick,
            enabled = enabled,
            variant = variant,
            tint = tint,
            modifier = modifier,
        )
    }
}

@Composable
private fun IconButtonContent(
    imageVector: ImageVector,
    contentDescription: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: PreviewLabIconButtonVariant = PreviewLabIconButtonVariant.Ghost,
    tint: Color? = null,
) = PreviewLabIconButton(variant = variant, onClick = onClick, enabled = enabled, modifier = modifier.size(28.dp)) {
    PreviewLabIcon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier.size(20.dp),
        tint = tint ?: LocalContentColor.current,
    )
}

@Composable
private fun IconButtonContent(
    painter: Painter,
    contentDescription: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: PreviewLabIconButtonVariant = PreviewLabIconButtonVariant.Ghost,
    tint: Color? = null,
) = PreviewLabIconButton(variant = variant, onClick = onClick, enabled = enabled, modifier = modifier.size(28.dp)) {
    PreviewLabIcon(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier.size(20.dp),
        tint = tint ?: LocalContentColor.current,
    )
}

@Composable
@InternalComposePreviewLabApi
fun PreviewLabIconButton(
    painter: Painter,
    contentDescription: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: PreviewLabIconButtonVariant = PreviewLabIconButtonVariant.Ghost,
    tint: Color? = null,
) = if (contentDescription == null) {
    IconButtonContent(
        painter = painter,
        contentDescription = contentDescription,
        onClick = onClick,
        enabled = enabled,
        variant = variant,
        tint = tint,
        modifier = modifier,
    )
} else {
    PreviewLabTooltipBox(
        tooltip = contentDescription,
    ) {
        IconButtonContent(
            painter = painter,
            contentDescription = contentDescription,
            onClick = onClick,
            enabled = enabled,
            variant = variant,
            tint = tint,
            modifier = modifier,
        )
    }
}

@Composable
@InternalComposePreviewLabApi
fun PreviewLabIconButtonContent(
    painter: Painter,
    contentDescription: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: PreviewLabIconButtonVariant = PreviewLabIconButtonVariant.Ghost,
    tint: Color? = null,
    enabled: Boolean = true,
) = PreviewLabIconButton(
    variant = variant,
    onClick = onClick,
    enabled = enabled,
    modifier = modifier.size(28.dp),
) {
    PreviewLabIcon(
        painter = painter,
        contentDescription = contentDescription,
        tint = tint ?: LocalContentColor.current,
        modifier = modifier.size(20.dp),
    )
}
