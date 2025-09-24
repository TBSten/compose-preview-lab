package me.tbsten.compose.preview.lab

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import java.util.Collections.emptyMap
import me.tbsten.compose.preview.lab.openfilehandler.OpenFileHandler

@Composable
fun ApplicationScope.PreviewLabRootWindows(
    previews: List<CollectedPreview>,
    openFileHandler: OpenFileHandler<out Any?>? = null,
    featuredFiles: Map<String, List<String>> = emptyMap(),
    // Window arguments
    // TODO: Review appropriate default values
    onCloseRequest: () -> Unit = ::exitApplication,
    state: WindowState = rememberWindowState(),
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
        state = state,
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
        PreviewLabRoot(
            previews = previews,
            featuredFiles = featuredFiles,
            openFileHandler = openFileHandler,
        )
    }
}
