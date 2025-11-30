package me.tbsten.compose.preview.lab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlin.js.ExperimentalWasmJsInterop
import me.tbsten.compose.preview.lab.openfilehandler.OpenFileHandler

@OptIn(ExperimentalWasmJsInterop::class)
expect fun List<PreviewLabPreview>.findEmbedded(
    isEmbeddedQueryName: String = "iframe",
    previewIdQueryName: String = "previewId",
): PreviewLabPreview?

@Composable
fun EmbeddedPreviewOrGallery(
    previewList: List<PreviewLabPreview>,
    modifier: Modifier = Modifier,
    isEmbeddedQueryName: String = "iframe",
    previewIdQueryName: String = "previewId",
    state: PreviewLabGalleryState = remember { PreviewLabGalleryState() },
    openFileHandler: OpenFileHandler<out Any?>? = null,
    featuredFileList: Map<String, List<String>> = emptyMap(),
) {
    previewList.findEmbedded(isEmbeddedQueryName = isEmbeddedQueryName, previewIdQueryName = previewIdQueryName)
        ?.let { selectedPreview ->
            CompositionLocalProvider(
                LocalDefaultIsHeaderShow provides false,
            ) {
                selectedPreview.content()
            }
        } ?: run {
        PreviewLabGallery(
            previewList = previewList,
            modifier = modifier,
            state = state,
            openFileHandler = openFileHandler,
            featuredFileList = featuredFileList,
        )
    }
}
