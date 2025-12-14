package me.tbsten.compose.preview.lab

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import me.tbsten.compose.preview.lab.gallery.LocalPreviewLabGalleryNavigator
import me.tbsten.compose.preview.lab.gallery.NoOpPreviewLabGalleryNavigator
import me.tbsten.compose.preview.lab.gallery.PreviewLabGalleryNavigator
import me.tbsten.compose.preview.lab.previewlab.openfilehandler.LocalOpenFileHandler
import me.tbsten.compose.preview.lab.previewlab.openfilehandler.OpenFileHandler
import me.tbsten.compose.preview.lab.util.JsOnlyExport
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.url.URLSearchParams

@OptIn(ExperimentalWasmJsInterop::class)
actual fun List<PreviewLabPreview>.findBySearchParam(previewIdQueryName: String): PreviewLabPreview? {
    val previewList = this
    val selectedPreviewId = URLSearchParams(window.location.search.toJsString()).get(previewIdQueryName)
    val selectedPreview = previewList.find { it.id == selectedPreviewId }

    return selectedPreview
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

@OptIn(ExperimentalWasmJsInterop::class, ExperimentalJsExport::class, ExperimentalWasmJsInterop::class)
@JsExport
actual fun isEmbedded(isEmbeddedSearchParamName: String): Boolean =
    URLSearchParams(window.location.search.toJsString()).has(isEmbeddedSearchParamName)
