package me.tbsten.compose.preview.lab.component

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import me.tbsten.compose.preview.lab.CollectedPreview
import me.tbsten.compose.preview.lab.OpenFileHandler
import me.tbsten.compose.preview.lab.PreviewLabRoot
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
