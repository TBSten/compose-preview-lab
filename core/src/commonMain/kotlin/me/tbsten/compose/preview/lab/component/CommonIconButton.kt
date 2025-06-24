package me.tbsten.compose.preview.lab.component

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ui.components.Icon
import me.tbsten.compose.preview.lab.ui.components.IconButton
import me.tbsten.compose.preview.lab.ui.components.IconButtonVariant
import me.tbsten.compose.preview.lab.ui.components.TooltipBox

@Composable
internal fun CommonIconButton(
    imageVector: ImageVector,
    contentDescription: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: IconButtonVariant = IconButtonVariant.Ghost,
) = if (contentDescription == null) {
    IconButtonContent(
        imageVector = imageVector,
        contentDescription = contentDescription,
        onClick = onClick,
        enabled = enabled,
        variant = variant,
        modifier = modifier,
    )
} else {
    TooltipBox(
        tooltip = contentDescription,
    ) {
        IconButtonContent(
            imageVector = imageVector,
            contentDescription = contentDescription,
            onClick = onClick,
            enabled = enabled,
            variant = variant,
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
    variant: IconButtonVariant = IconButtonVariant.Ghost,
) = IconButton(variant = variant, onClick = onClick, enabled = enabled, modifier = modifier.size(28.dp)) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier.size(20.dp),
    )
}

@Composable
internal fun CommonIconButton(
    painter: Painter,
    contentDescription: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: IconButtonVariant = IconButtonVariant.Ghost,
) = if (contentDescription == null) {
    IconButtonContent(
        painter = painter,
        contentDescription = contentDescription,
        onClick = onClick,
        enabled = enabled,
        variant = variant,
        modifier = modifier,
    )
} else {
    TooltipBox(
        tooltip = contentDescription,
    ) {
        IconButtonContent(
            painter = painter,
            contentDescription = contentDescription,
            onClick = onClick,
            enabled = enabled,
            variant = variant,
            modifier = modifier,
        )
    }
}

@Composable
private fun IconButtonContent(
    painter: Painter,
    contentDescription: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: IconButtonVariant = IconButtonVariant.Ghost,
    enabled: Boolean = true,
) = IconButton(
    variant = variant,
    onClick = onClick,
    enabled = enabled,
    modifier = modifier.size(28.dp),
) {
    Icon(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier.size(20.dp),
    )
}
