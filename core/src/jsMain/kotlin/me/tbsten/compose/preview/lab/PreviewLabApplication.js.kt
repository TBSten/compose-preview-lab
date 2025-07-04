package me.tbsten.compose.preview.lab

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import me.tbsten.compose.preview.lab.openfilehandler.OpenFileHandler
import org.w3c.dom.HTMLElement

/**
 * Preview the [PreviewLabRoot] component in a web environment.
 *
 * ```kt
 * fun main() = previewLabApplication(
 *     previews = myModule.previews,
 * )
 * ```
 *
 * @see PreviewLabRoot
 */
@OptIn(ExperimentalComposeUiApi::class)
fun previewLabApplication(
    previews: List<CollectedPreview>,
    featuredFiles: Map<String, List<String>> = emptyMap(),
    openFileHandler: OpenFileHandler<out Any?>? = null,
    rootElement: HTMLElement = document.body!!,
) {
    ComposeViewport(rootElement) {
        PreviewLabRoot(
            previews = previews,
            featuredFiles = featuredFiles,
            openFileHandler = openFileHandler,
        )
    }
}
