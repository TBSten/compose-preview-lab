package me.tbsten.compose.preview.lab.me.component

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import me.tbsten.compose.preview.lab.me.CollectedPreview
import me.tbsten.compose.preview.lab.me.OpenFileHandler
import me.tbsten.compose.preview.lab.me.PreviewLabRoot
import org.w3c.dom.HTMLElement

@OptIn(ExperimentalComposeUiApi::class)
fun previewLabApplication(
    previews: List<CollectedPreview>,
    openFileHandler: OpenFileHandler? = null,
    rootElement: HTMLElement = document.body!!,
) {
    ComposeViewport(rootElement) {
        PreviewLabRoot(
            previews = previews,
            openFileHandler = openFileHandler,
        )
    }
}
