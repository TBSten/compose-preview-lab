package me.tbsten.compose.preview.lab

import kotlinx.browser.window
import org.w3c.dom.url.URLSearchParams

@OptIn(markerClass = [ExperimentalWasmJsInterop::class])
actual fun List<PreviewLabPreview>.findEmbedded(isEmbeddedQueryName: String, previewIdQueryName: String,): PreviewLabPreview? {
    val previewList = this
    val isIframeEmbedded = URLSearchParams(window.location.search.toJsString()).has(isEmbeddedQueryName)
    val selectedPreviewId = URLSearchParams(window.location.search.toJsString()).get(previewIdQueryName)
    val selectedPreview = previewList.find { it.id == selectedPreviewId }

    return selectedPreview.takeIf { isIframeEmbedded }
}
