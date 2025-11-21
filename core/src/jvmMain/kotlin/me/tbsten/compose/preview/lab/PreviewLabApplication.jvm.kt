package me.tbsten.compose.preview.lab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import java.util.Collections.emptyMap
import me.tbsten.compose.preview.lab.openfilehandler.OpenFileHandler

/**
 * Creates a desktop window application for previewing Compose components
 *
 * Launches a JVM desktop application with PreviewLab interface in a native window.
 * Provides full desktop integration with customizable window properties for
 * interactive component development and testing.
 *
 * ```kotlin
 * // Basic desktop application
 * fun main() = application {
 *     PreviewLabGalleryWindows(
 *         previewList = myModule.PreviewList
 *     )
 * }
 *
 * // With file handler and featured files
 * fun main() = application {
 *     PreviewLabGalleryWindows(
 *         previewList = myModule.PreviewList,
 *         openFileHandler = UrlOpenFileHandler("https://github.com/user/repo/blob/main"),
 *         featuredFileList = myModule.FeaturedFileList,
 *     )
 * }
 *
 * // Custom window configuration
 * fun main() = application {
 *     PreviewLabGalleryWindows(
 *         previewList = myModule.PreviewList,
 *         windowState = rememberWindowState(size = DpSize(1400.dp, 900.dp)),
 *         title = "My Component Gallery",
 *     )
 * }
 * ```
 *
 * @param previewList Collection of previews to display in the interface
 * @param openFileHandler Handler for opening source files (optional)
 * @param featuredFileList Grouped file organization for navigation
 * @param state PreviewLabGalleryState for managing gallery state
 * @param windowState WindowState for managing window properties
 * @param onCloseRequest Callback invoked when window close is requested
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
 * @see PreviewLabGallery
 * @see CollectedPreview
 * @see OpenFileHandler
 */
@Composable
fun ApplicationScope.PreviewLabGalleryWindows(
    previewList: List<PreviewLabPreview>,
    openFileHandler: OpenFileHandler<out Any?>? = null,
    featuredFileList: Map<String, List<String>> = emptyMap(),
    state: PreviewLabGalleryState = remember { PreviewLabGalleryState() },
    // Window arguments
    // TODO: Review appropriate default values
    windowState: WindowState = rememberWindowState(size = DpSize(1000.dp, 800.dp)),
    onCloseRequest: () -> Unit = ::exitApplication,
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
) {
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
        PreviewLabGallery(
            previewList = previewList,
            featuredFileList = featuredFileList,
            openFileHandler = openFileHandler,
            state = state,
        )
    }
}
