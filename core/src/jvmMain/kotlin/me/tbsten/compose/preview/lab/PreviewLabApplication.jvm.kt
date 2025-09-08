package me.tbsten.compose.preview.lab

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.util.Collections.emptyMap
import me.tbsten.compose.preview.lab.openfilehandler.OpenFileHandler

/**
 * Launches a desktop application for previewing Compose components
 * 
 * Creates a native desktop window running the PreviewLab interface for interactive
 * component development and testing. Provides comprehensive window customization options
 * including size, position, decorations, and behavior. Integrates with file opening
 * handlers for source code navigation and supports featured file grouping.
 * 
 * ```kotlin
 * // Basic application launch
 * fun main() = previewLabApplication(
 *     previews = myModule.previews
 * )
 * 
 * // Advanced configuration with file handler
 * fun main() = previewLabApplication(
 *     previews = myModule.previews,
 *     openFileHandler = GithubOpenFileHandler("user/repo"),
 *     featuredFiles = mapOf(
 *         "Components" to listOf("Button.kt", "Card.kt"),
 *         "Layouts" to listOf("Grid.kt", "List.kt")
 *     ),
 *     title = "My App Preview Lab",
 *     width = 1200.dp,
 *     height = 800.dp
 * )
 * 
 * // Custom window behavior
 * fun main() = previewLabApplication(
 *     previews = myModule.previews,
 *     alwaysOnTop = true,
 *     resizable = false,
 *     onCloseRequest = { 
 *         // Custom cleanup
 *         exitApplication()
 *     }
 * )
 * ```
 * 
 * @param previews Collection of previews to display in the interface
 * @param openFileHandler Handler for opening source files (optional)
 * @param featuredFiles Grouped file organization for navigation
 * @param onCloseRequest Action to perform when window is closed
 * @param visible Whether window is initially visible
 * @param title Window title bar text
 * @param icon Window icon (optional)
 * @param undecorated Whether to hide window decorations
 * @param transparent Whether window background is transparent
 * @param resizable Whether window can be resized
 * @param enabled Whether window accepts input
 * @param focusable Whether window can receive focus
 * @param alwaysOnTop Whether window stays above others
 * @param onPreviewKeyEvent Key event handler (preview phase)
 * @param onKeyEvent Key event handler (bubble phase)
 * @param windowConfiguration Additional window configuration
 * @param placement Window placement mode
 * @param isMinimized Whether window starts minimized
 * @param position Initial window position
 * @param width Initial window width
 * @param height Initial window height
 * @see PreviewLabRoot
 * @see CollectedPreview
 * @see OpenFileHandler
 */
fun previewLabApplication(
    previews: List<CollectedPreview>,
    openFileHandler: OpenFileHandler<out Any?>? = null,
    featuredFiles: Map<String, List<String>> = emptyMap(),
    // Window arguments
    onCloseRequest: ApplicationScope.() -> Unit = { exitApplication() },
    visible: Boolean = true,
    title: String = "Compose Preview Lab",
    icon: Painter? = null,
    undecorated: Boolean = false,
    transparent: Boolean = false,
    resizable: Boolean = true,
    enabled: Boolean = true,
    focusable: Boolean = true,
    alwaysOnTop: Boolean = false,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    windowConfiguration: @Composable FrameWindowScope.() -> Unit = { },
    // rememberWindowState arguments
    placement: WindowPlacement = WindowPlacement.Floating,
    isMinimized: Boolean = false,
    position: WindowPosition = WindowPosition.PlatformDefault,
    width: Dp = 840.dp,
    height: Dp = 600.dp,
) = application {
    Window(
        onCloseRequest = { onCloseRequest() },
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
        state = rememberWindowState(
            placement = placement,
            isMinimized = isMinimized,
            position = position,
            width = width,
            height = height,
        ),
    ) {
        windowConfiguration()
        PreviewLabRoot(
            previews = previews,
            featuredFiles = featuredFiles,
            openFileHandler = openFileHandler,
        )
    }
}
