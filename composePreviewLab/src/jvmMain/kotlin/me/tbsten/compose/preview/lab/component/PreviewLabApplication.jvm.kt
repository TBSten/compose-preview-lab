package me.tbsten.compose.preview.lab.component

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
import me.tbsten.compose.preview.lab.CollectedPreview
import me.tbsten.compose.preview.lab.OpenFileHandler
import me.tbsten.compose.preview.lab.PreviewLabRoot

fun previewLabApplication(
    previews: List<CollectedPreview>,
    openFileHandler: OpenFileHandler? = null,
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
    width: Dp = 800.dp,
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
            openFileHandler = openFileHandler,
        )
    }
}
