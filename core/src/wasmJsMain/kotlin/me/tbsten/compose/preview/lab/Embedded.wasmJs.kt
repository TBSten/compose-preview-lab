package me.tbsten.compose.preview.lab

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import me.tbsten.compose.preview.lab.openfilehandler.LocalOpenFileHandler
import me.tbsten.compose.preview.lab.openfilehandler.OpenFileHandler
import me.tbsten.compose.preview.lab.util.JsOnlyExport
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.url.URLSearchParams

@OptIn(ExperimentalWasmJsInterop::class)
actual fun List<PreviewLabPreview>.findEmbedded(isEmbeddedQueryName: String, previewIdQueryName: String): PreviewLabPreview? {
    val previewList = this
    val isIframeEmbedded = URLSearchParams(window.location.search.toJsString()).has(isEmbeddedQueryName)
    val selectedPreviewId = URLSearchParams(window.location.search.toJsString()).get(previewIdQueryName)
    val selectedPreview = previewList.find { it.id == selectedPreviewId }

    return selectedPreview.takeIf { isIframeEmbedded }
}

typealias DisposePreviewLabPreviewElements = () -> Unit

@OptIn(ExperimentalJsExport::class, ExperimentalComposeUiApi::class)
@ExperimentalComposePreviewLabApi
@JsOnlyExport
fun renderPreviewLabPreview(
    rootElement: Element,
    preview: PreviewLabPreview,
    openFileHandler: OpenFileHandler<out Any?>? = null,
    navigator: PreviewLabGalleryNavigator = NoOpPreviewLabGalleryNavigator,
): DisposePreviewLabPreviewElements {
    val containerElement = (document.createElement("div") as HTMLDivElement).apply {
        appendChild(rootElement)
    }

    ComposeViewport(containerElement) {
        CompositionLocalProvider(
            LocalOpenFileHandler provides openFileHandler,
            LocalPreviewLabGalleryNavigator provides navigator,
        ) {
            preview.content()
        }
    }

    return { containerElement.remove() }
}
