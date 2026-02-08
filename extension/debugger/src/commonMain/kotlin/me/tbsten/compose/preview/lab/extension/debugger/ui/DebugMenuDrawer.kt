package me.tbsten.compose.preview.lab.extension.debugger.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.extension.debugger.DebugMenu
import me.tbsten.compose.preview.lab.ui.adaptive
import me.tbsten.compose.preview.lab.ui.components.PreviewLabIcon
import me.tbsten.compose.preview.lab.ui.components.PreviewLabText
import me.tbsten.compose.preview.lab.ui.generated.resources.PreviewLabUiRes
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_arrow_drop_down
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_arrow_left
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_arrow_right
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_close
import org.jetbrains.compose.resources.painterResource

/**
 * Position of the debug menu drawer.
 */
enum class DebugMenuDrawerPosition {
    /** Drawer slides in from the right side */
    Right,

    /** Drawer slides in from the left side */
    Left,

    /** Drawer slides in from the bottom */
    Bottom,
}

/**
 * Displays a drawer for the [DebugMenu].
 *
 * This drawer slides in from the specified position and allows
 * interaction with the app content behind it (non-modal).
 * Users can change the drawer position at runtime using the position
 * selector in the title bar.
 *
 * Example usage:
 * ```kotlin
 * @Composable
 * fun App() {
 *     var showDebugDrawer by remember { mutableStateOf(false) }
 *
 *     Box(modifier = Modifier.fillMaxSize()) {
 *         // Your app content
 *         AppContent()
 *
 *         // Debug drawer (non-modal, app remains interactive)
 *         DebugMenuDrawer(
 *             debugMenu = AppDebugMenu,
 *             visible = showDebugDrawer,
 *             onCloseRequest = { showDebugDrawer = false },
 *         )
 *     }
 *
 *     // Trigger to toggle drawer
 *     DebugMenuTrigger(onTrigger = { showDebugDrawer = !showDebugDrawer })
 * }
 * ```
 *
 * @param debugMenu The debug menu instance to display
 * @param visible Whether the drawer is visible
 * @param onCloseRequest Callback invoked when the drawer close is requested
 * @param initialPosition Initial position of the drawer (Right, Left, or Bottom)
 * @param title Drawer title
 * @param size Drawer width (for Left/Right) or height (for Bottom) in dp
 */
@Composable
fun DebugMenuDrawer(
    debugMenu: DebugMenu,
    visible: Boolean,
    onCloseRequest: () -> Unit,
    initialPosition: DebugMenuDrawerPosition = adaptive(
        small = DebugMenuDrawerPosition.Bottom,
        medium = DebugMenuDrawerPosition.Right,
    ),
    title: String = "Debug Menu",
    size: Dp = 360.dp,
) {
    var position by rememberSaveable { mutableStateOf(initialPosition) }

    val backgroundColor = DebugMenuTheme.backgroundColor
    val contentColor = DebugMenuTheme.contentColor

    val alignment = when (position) {
        DebugMenuDrawerPosition.Right -> Alignment.CenterEnd
        DebugMenuDrawerPosition.Left -> Alignment.CenterStart
        DebugMenuDrawerPosition.Bottom -> Alignment.BottomCenter
    }

    val shape = when (position) {
        DebugMenuDrawerPosition.Right -> RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
        DebugMenuDrawerPosition.Left -> RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
        DebugMenuDrawerPosition.Bottom -> RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
    }

    val sizeModifier = when (position) {
        DebugMenuDrawerPosition.Right, DebugMenuDrawerPosition.Left ->
            Modifier.fillMaxHeight().width(size)
        DebugMenuDrawerPosition.Bottom ->
            Modifier.fillMaxWidth().height(size)
    }

    val enterTransition = when (position) {
        DebugMenuDrawerPosition.Right -> slideInHorizontally(initialOffsetX = { it })
        DebugMenuDrawerPosition.Left -> slideInHorizontally(initialOffsetX = { -it })
        DebugMenuDrawerPosition.Bottom -> slideInVertically(initialOffsetY = { it })
    }

    val exitTransition = when (position) {
        DebugMenuDrawerPosition.Right -> slideOutHorizontally(targetOffsetX = { it })
        DebugMenuDrawerPosition.Left -> slideOutHorizontally(targetOffsetX = { -it })
        DebugMenuDrawerPosition.Bottom -> slideOutVertically(targetOffsetY = { it })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = enterTransition,
            exit = exitTransition,
            modifier = Modifier.align(alignment),
        ) {
            Box(
                modifier = sizeModifier
                    .shadow(8.dp, shape)
                    .clip(shape)
                    .background(backgroundColor)
                    .border(1.dp, contentColor.copy(alpha = 0.2f), shape),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Title bar with position selector
                    DrawerTitleBar(
                        title = title,
                        currentPosition = position,
                        onPositionChange = { position = it },
                        onCloseRequest = onCloseRequest,
                    )

                    // Debug menu content
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                    ) {
                        debugMenu.View(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerTitleBar(
    title: String,
    currentPosition: DebugMenuDrawerPosition,
    onPositionChange: (DebugMenuDrawerPosition) -> Unit,
    onCloseRequest: () -> Unit,
) {
    val contentColor = DebugMenuTheme.contentColor
    var showPositionSelector by remember { mutableStateOf(false) }

    Column {
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

            // Position selector button
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { showPositionSelector = !showPositionSelector },
                contentAlignment = Alignment.Center,
            ) {
                PreviewLabIcon(
                    painter = painterResource(
                        when (currentPosition) {
                            DebugMenuDrawerPosition.Right -> PreviewLabUiRes.drawable.icon_arrow_right
                            DebugMenuDrawerPosition.Left -> PreviewLabUiRes.drawable.icon_arrow_left
                            DebugMenuDrawerPosition.Bottom -> PreviewLabUiRes.drawable.icon_arrow_drop_down
                        },
                    ),
                    contentDescription = "Change position",
                )
            }

            // Close button
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = onCloseRequest),
                contentAlignment = Alignment.Center,
            ) {
                PreviewLabIcon(
                    painter = painterResource(PreviewLabUiRes.drawable.icon_close),
                    contentDescription = "Close",
                )
            }
        }

        // Position selector dropdown
        AnimatedVisibility(visible = showPositionSelector) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(contentColor.copy(alpha = 0.03f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DebugMenuDrawerPosition.entries.forEach { pos ->
                    PositionButton(
                        position = pos,
                        isSelected = pos == currentPosition,
                        onClick = {
                            onPositionChange(pos)
                            showPositionSelector = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PositionButton(position: DebugMenuDrawerPosition, isSelected: Boolean, onClick: () -> Unit) {
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
            painter = painterResource(
                when (position) {
                    DebugMenuDrawerPosition.Right -> PreviewLabUiRes.drawable.icon_arrow_right
                    DebugMenuDrawerPosition.Left -> PreviewLabUiRes.drawable.icon_arrow_left
                    DebugMenuDrawerPosition.Bottom -> PreviewLabUiRes.drawable.icon_arrow_drop_down
                },
            ),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
        PreviewLabText(
            text = when (position) {
                DebugMenuDrawerPosition.Right -> "Right"
                DebugMenuDrawerPosition.Left -> "Left"
                DebugMenuDrawerPosition.Bottom -> "Bottom"
            },
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}
