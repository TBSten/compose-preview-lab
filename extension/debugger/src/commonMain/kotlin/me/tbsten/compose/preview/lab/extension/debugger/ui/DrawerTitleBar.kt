package me.tbsten.compose.preview.lab.extension.debugger.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ui.components.PreviewLabIcon
import me.tbsten.compose.preview.lab.ui.components.PreviewLabText
import me.tbsten.compose.preview.lab.ui.generated.resources.PreviewLabUiRes
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_close
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun DrawerTitleBar(
    title: String,
    currentPosition: DebugMenuDrawerPosition,
    onPositionChange: (DebugMenuDrawerPosition) -> Unit,
    onCloseRequest: () -> Unit,
) {
    var showPositionSelector by remember { mutableStateOf(false) }

    Column {
        TitleRow(
            title = title,
            currentPosition = currentPosition,
            onTogglePositionSelector = { showPositionSelector = !showPositionSelector },
            onCloseRequest = onCloseRequest,
        )

        PositionSelectorRow(
            visible = showPositionSelector,
            currentPosition = currentPosition,
            onPositionChange = {
                onPositionChange(it)
                showPositionSelector = false
            },
        )
    }
}

@Composable
private fun TitleRow(
    title: String,
    currentPosition: DebugMenuDrawerPosition,
    onTogglePositionSelector: () -> Unit,
    onCloseRequest: () -> Unit,
) {
    val contentColor = DebugMenuTheme.contentColor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(contentColor.copy(alpha = 0.05f))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PreviewLabText(
            text = title,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )

        PositionSelectorButton(
            currentPosition = currentPosition,
            onClick = onTogglePositionSelector,
        )

        CloseButton(onClick = onCloseRequest)
    }
}

@Composable
private fun PositionSelectorButton(currentPosition: DebugMenuDrawerPosition, onClick: () -> Unit,) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        PreviewLabIcon(
            painter = painterResource(currentPosition.iconRes),
            contentDescription = "Change position",
        )
    }
}

@Composable
private fun CloseButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        PreviewLabIcon(
            painter = painterResource(PreviewLabUiRes.drawable.icon_close),
            contentDescription = "Close",
        )
    }
}

@Composable
private fun PositionSelectorRow(
    visible: Boolean,
    currentPosition: DebugMenuDrawerPosition,
    onPositionChange: (DebugMenuDrawerPosition) -> Unit,
) {
    val contentColor = DebugMenuTheme.contentColor

    AnimatedVisibility(visible = visible) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(contentColor.copy(alpha = 0.03f))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DebugMenuDrawerPosition.entries.forEach { position ->
                PositionButton(
                    position = position,
                    isSelected = position == currentPosition,
                    onClick = { onPositionChange(position) },
                )
            }
        }
    }
}

@Composable
private fun PositionButton(position: DebugMenuDrawerPosition, isSelected: Boolean, onClick: () -> Unit,) {
    val contentColor = DebugMenuTheme.contentColor
    val backgroundColor = if (isSelected) {
        contentColor.copy(alpha = 0.15f)
    } else {
        contentColor.copy(alpha = 0.05f)
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        PreviewLabIcon(
            painter = painterResource(position.iconRes),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
        PreviewLabText(
            text = position.label,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}
