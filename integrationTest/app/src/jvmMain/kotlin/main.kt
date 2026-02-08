@file:Suppress("ktlint:standard:filename")

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import me.tbsten.compose.preview.lab.PreviewLabGalleryWindows
import me.tbsten.compose.preview.lab.extension.debugger.DebugMenuWindow
import me.tbsten.compose.preview.lab.gallery.PreviewLabGalleryState
import me.tbsten.compose.preview.lab.previewlab.openfilehandler.UrlOpenFileHandler
import me.tbsten.compose.preview.lab.sample.debugmenu.AppDebugMenu

fun main(): Unit = application {
    var showDebugMenu by remember { mutableStateOf(true) }
    val mainWindowState = rememberWindowState(size = DpSize(1000.dp, 800.dp))

    PreviewLabGalleryWindows(
        previewList = app.PreviewList + uiLib.PreviewList,
        featuredFileList = app.FeaturedFileList,
        openFileHandler = UrlOpenFileHandler(
            baseUrl = "https://github.com/TBSten/compose-preview-lab/blob/main/integrationTest/",
        ),
        state = remember {
            PreviewLabGalleryState()
        },
        windowState = mainWindowState,
    )

    if (showDebugMenu) {
        DebugMenuWindow(
            debugMenu = AppDebugMenu,
            baseWindowState = mainWindowState,
            onCloseRequest = { showDebugMenu = false },
            title = "App Debug Menu",
        )
    }
}
