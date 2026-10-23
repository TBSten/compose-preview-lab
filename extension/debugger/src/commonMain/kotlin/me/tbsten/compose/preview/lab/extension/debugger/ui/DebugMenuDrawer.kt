package me.tbsten.compose.preview.lab.extension.debugger.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.extension.debugger.DebugMenu
import me.tbsten.compose.preview.lab.ui.adaptive

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

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = position.enterTransition,
            exit = position.exitTransition,
            modifier = Modifier.align(position.alignment),
        ) {
            DrawerContent(
                debugMenu = debugMenu,
                title = title,
                position = position,
                onPositionChange = { position = it },
                onCloseRequest = onCloseRequest,
                backgroundColor = backgroundColor,
                contentColor = contentColor,
                size = size,
            )
        }
    }
}

@Composable
private fun DrawerContent(
    debugMenu: DebugMenu,
    title: String,
    position: DebugMenuDrawerPosition,
    onPositionChange: (DebugMenuDrawerPosition) -> Unit,
    onCloseRequest: () -> Unit,
    backgroundColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    size: Dp,
) {
    val shape = position.shape

    Box(
        modifier = position.sizeModifier(size)
            .shadow(8.dp, shape)
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, contentColor.copy(alpha = 0.2f), shape),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DrawerTitleBar(
                title = title,
                currentPosition = position,
                onPositionChange = onPositionChange,
                onCloseRequest = onCloseRequest,
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            ) {
                debugMenu.TabsView(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
