package me.tbsten.compose.preview.lab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlin.js.ExperimentalWasmJsInterop
import me.tbsten.compose.preview.lab.openfilehandler.OpenFileHandler

@OptIn(ExperimentalWasmJsInterop::class)
expect fun List<PreviewLabPreview>.findBySearchParam(previewIdQueryName: String = "previewId"): PreviewLabPreview?

expect fun isEmbedded(isEmbeddedSearchParamName: String = "iframe"): Boolean

fun initialSelectedPreviewFromSearchParam(
    previewList: List<PreviewLabPreview>,
    previewIdQueryName: String = "previewId",
    groupName: String = AllGroupName,
) = previewList.findBySearchParam(previewIdQueryName = previewIdQueryName)
    ?.let { groupName to it }

@Composable
fun EmbeddedPreviewOrGallery(
    previewList: List<PreviewLabPreview>,
    modifier: Modifier = Modifier,
    isEmbeddedSearchParamName: String = "iframe",
    previewIdQueryName: String = "previewId",
    state: PreviewLabGalleryState = remember {
        PreviewLabGalleryState(
            initialSelectedPreview = initialSelectedPreviewFromSearchParam(previewList, previewIdQueryName),
        )
    },
    openFileHandler: OpenFileHandler<out Any?>? = null,
    featuredFileList: Map<String, List<String>> = emptyMap(),
) {
    val selectedPreview = state.selectedPreview

    if (isEmbedded(isEmbeddedSearchParamName) && selectedPreview != null) {
        CompositionLocalProvider(
            LocalDefaultIsHeaderShow provides false,
        ) {
            selectedPreview.preview.content()
        }
    } else {
        PreviewLabGallery(
            previewList = previewList,
            modifier = modifier,
            state = state,
            openFileHandler = openFileHandler,
            featuredFileList = featuredFileList,
        )
    }
}
