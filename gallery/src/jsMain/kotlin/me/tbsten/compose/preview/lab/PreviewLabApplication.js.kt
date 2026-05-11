package me.tbsten.compose.preview.lab

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import me.tbsten.compose.preview.lab.gallery.PreviewLabGalleryState
import me.tbsten.compose.preview.lab.previewlab.openfilehandler.OpenFileHandler
import org.w3c.dom.HTMLElement

/**
 * Mounts [PreviewLabGallery] into a [ComposeViewport] in the browser (Compose for Web /
 * Kotlin/JS target).
 *
 * ```kotlin
 * fun main() = previewLabApplication(
 *     previewList = myModule.PreviewList,
 *     openFileHandler = UrlOpenFileHandler("https://github.com/user/repo/blob/main"),
 *     featuredFileList = app.FeaturedFileList,
 * )
 *
 * // Mount into a custom container element instead of <body>.
 * fun main() = previewLabApplication(
 *     previewList = myModule.PreviewList,
 *     rootElement = document.getElementById("preview-container") as HTMLElement,
 * )
 * ```
 *
 * @param rootElement HTML element to mount into; defaults to `document.body`.
 * @see me.tbsten.compose.preview.lab.gallery.PreviewLabGallery
 * @see OpenFileHandler
 */
@OptIn(ExperimentalComposeUiApi::class)
fun previewLabApplication(
    previewList: List<PreviewLabPreview>,
    featuredFileList: Map<String, List<String>> = emptyMap(),
    openFileHandler: OpenFileHandler<out Any?>? = null,
    state: PreviewLabGalleryState = PreviewLabGalleryState(),
    rootElement: HTMLElement = document.body!!,
) {
    ComposeViewport(rootElement) {
        EmbeddedPreviewOrGallery(
            previewList = previewList,
            featuredFileList = featuredFileList,
            openFileHandler = openFileHandler,
            state = state,
        )
    }
}
