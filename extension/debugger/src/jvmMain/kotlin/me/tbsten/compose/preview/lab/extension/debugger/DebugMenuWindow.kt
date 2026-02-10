package me.tbsten.compose.preview.lab.extension.debugger

import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.movableContentWithReceiverOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.flow.combine
import me.tbsten.compose.preview.lab.extension.debugger.ui.TabsView

/**
 * Creates a desktop window for displaying a [DebugMenu].
 *
 * This function provides a convenient way to show a debug menu in a separate
 * window on JVM desktop applications. The window can be customized with
 * standard Compose Desktop window parameters.
 *
 * When [baseWindowState] is provided, this window will automatically position
 * itself to the right of the base window and follow its movements.
 *
 * Example usage:
 * ```kotlin
 * fun main() = application {
 *     val mainWindowState = rememberWindowState()
 *
 *     // Main application window
 *     Window(
 *         onCloseRequest = ::exitApplication,
 *         state = mainWindowState,
 *     ) {
 *         MyApp()
 *     }
 *
 *     // Debug menu window (follows main window)
 *     DebugMenuWindow(
 *         debugMenu = AppDebugMenu,
 *         baseWindowState = mainWindowState,
 *         onCloseRequest = { /* hide window or exit */ },
 *     )
 * }
 * ```
 *
 * @param debugMenu The debug menu instance to display
 * @param onCloseRequest Callback invoked when the window close is requested
 * @param baseWindowState Optional WindowState of the base window to follow.
 *   When provided, this window will position itself to the right of the base window.
 * @param windowState WindowState for managing this window's properties
 * @param visible Whether the window is visible
 * @param title Window title text
 * @param icon Window icon painter
 * @param undecorated Whether to remove window decorations
 * @param transparent Whether the window background is transparent
 * @param resizable Whether the window can be resized
 * @param enabled Whether the window is enabled for user interaction
 * @param focusable Whether the window can receive focus
 * @param alwaysOnTop Whether the window should stay on top of other windows
 * @param onPreviewKeyEvent Callback for preview key events
 * @param onKeyEvent Callback for key events
 */
@Composable
fun ApplicationScope.DebugMenuWindow(
    debugMenu: DebugMenu,
    onCloseRequest: () -> Unit,
    baseWindowState: WindowState? = null,
    windowState: WindowState = rememberWindowState(size = DpSize(400.dp, 600.dp)),
    visible: Boolean = true,
    title: String = "Debug Menu",
    icon: Painter? = null,
    undecorated: Boolean = false,
    transparent: Boolean = false,
    resizable: Boolean = true,
    enabled: Boolean = true,
    focusable: Boolean = true,
    alwaysOnTop: Boolean = false,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    content: @Composable DebugMenu.() -> Unit = { TabsView() },
) {
    FollowWindowPosition(baseWindowState, windowState)

    Window(
        onCloseRequest = onCloseRequest,
        state = windowState,
        visible = visible,
        title = title,
        icon = icon,
        undecorated = undecorated,
        transparent = transparent,
        resizable = resizable,
        enabled = enabled,
        focusable = focusable,
        alwaysOnTop = alwaysOnTop,
        onPreviewKeyEvent = onPreviewKeyEvent,
        onKeyEvent = onKeyEvent,
    ) {
        val viewContent = remember {
            movableContentWithReceiverOf<DebugMenu> {
                val debugMenu = this@movableContentWithReceiverOf

                content(debugMenu)
            }
        }
        if (undecorated) {
            WindowDraggableArea {
                debugMenu.viewContent()
            }
        } else {
            debugMenu.viewContent()
        }
    }
}

/**
 * Creates a desktop window for displaying this [DebugMenu].
 *
 * This is a convenience extension that allows calling `DebugMenuWindow` directly on a [DebugMenu] instance.
 *
 * @see DebugMenuWindow
 */
@Deprecated(
    message = "Use DebugMenuWindow(debugMenu = ...) instead",
    replaceWith = ReplaceWith(
        "DebugMenuWindow(debugMenu = this, onCloseRequest = onCloseRequest, baseWindowState = baseWindowState, " +
            "windowState = windowState, visible = visible, title = title, icon = icon, undecorated = undecorated, " +
            "transparent = transparent, resizable = resizable, enabled = enabled, focusable = focusable, " +
            "alwaysOnTop = alwaysOnTop, onPreviewKeyEvent = onPreviewKeyEvent, onKeyEvent = onKeyEvent)",
        "me.tbsten.compose.preview.lab.extension.debugger.DebugMenuWindow",
    ),
)
@Composable
fun DebugMenu.Window(
    applicationScope: ApplicationScope,
    onCloseRequest: () -> Unit,
    baseWindowState: WindowState? = null,
    windowState: WindowState = rememberWindowState(size = DpSize(400.dp, 600.dp)),
    visible: Boolean = true,
    title: String = "Debug Menu",
    icon: Painter? = null,
    undecorated: Boolean = false,
    transparent: Boolean = false,
    resizable: Boolean = true,
    enabled: Boolean = true,
    focusable: Boolean = true,
    alwaysOnTop: Boolean = false,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
) {
    with(applicationScope) {
        DebugMenuWindow(
            debugMenu = this@Window,
            onCloseRequest = onCloseRequest,
            baseWindowState = baseWindowState,
            windowState = windowState,
            visible = visible,
            title = title,
            icon = icon,
            undecorated = undecorated,
            transparent = transparent,
            resizable = resizable,
            enabled = enabled,
            focusable = focusable,
            alwaysOnTop = alwaysOnTop,
            onPreviewKeyEvent = onPreviewKeyEvent,
            onKeyEvent = onKeyEvent,
        )
    }
}

/**
 * Composable that follows the position of a base window.
 *
 * When [baseWindowState] is provided, [windowState] will be updated
 * to position itself to the right of the base window with a 20.dp gap.
 *
 * @param baseWindowState The window state to follow
 * @param windowState The window state to update
 */
@Composable
private fun FollowWindowPosition(baseWindowState: WindowState?, windowState: WindowState) {
    if (baseWindowState != null) {
        LaunchedEffect(baseWindowState) {
            combine(
                snapshotFlow { baseWindowState.position },
                snapshotFlow { baseWindowState.size },
            ) { position, size ->
                position to size
            }.collect { (position, size) ->
                if (position is WindowPosition.Absolute) {
                    windowState.position = WindowPosition.Absolute(
                        x = position.x + size.width + 20.dp,
                        y = position.y,
                    )
                }
            }
        }
    }
}
