package me.tbsten.compose.preview.lab.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CommonIconButton(
    imageVector: ImageVector,
    contentDescription: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) = if (contentDescription == null) {
    IconButtonContent(
        imageVector = imageVector,
        contentDescription = contentDescription,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
    )
} else {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(contentDescription) } },
        state = rememberTooltipState()
    ) {
        IconButtonContent(
            imageVector = imageVector,
            contentDescription = contentDescription,
            onClick = onClick,
            enabled = enabled,
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
) = IconButton(onClick = onClick, enabled = enabled, modifier = modifier.size(28.dp)) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier.size(20.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CommonIconButton(
    painter: Painter,
    contentDescription: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) = if (contentDescription == null) {
    IconButtonContent(
        painter = painter,
        contentDescription = contentDescription,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
    )
} else {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(contentDescription) } },
        state = rememberTooltipState()
    ) {
        IconButtonContent(
            painter = painter,
            contentDescription = contentDescription,
            onClick = onClick,
            enabled = enabled,
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
    enabled: Boolean = true,
) = IconButton(onClick = onClick, enabled = enabled, modifier = modifier.size(28.dp)) {
    Icon(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier.size(20.dp),
    )
}
