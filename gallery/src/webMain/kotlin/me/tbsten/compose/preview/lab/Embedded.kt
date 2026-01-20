package me.tbsten.compose.preview.lab

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.js.ExperimentalWasmJsInterop
import me.tbsten.compose.preview.lab.gallery.AllGroupName
import me.tbsten.compose.preview.lab.gallery.PreviewLabGallery
import me.tbsten.compose.preview.lab.gallery.PreviewLabGalleryState
import me.tbsten.compose.preview.lab.gallery.PreviewListGrid
import me.tbsten.compose.preview.lab.previewlab.openfilehandler.OpenFileHandler
import me.tbsten.compose.preview.lab.previewlab.LocalDefaultIsHeaderShow
import me.tbsten.compose.preview.lab.ui.adaptive

@OptIn(ExperimentalWasmJsInterop::class)
public expect fun List<PreviewLabPreview>.findBySearchParam(previewIdQueryName: String = "previewId"): PreviewLabPreview?

public expect fun isEmbedded(isEmbeddedSearchParamName: String = "iframe"): Boolean

public fun initialSelectedPreviewFromSearchParam(
    previewList: List<PreviewLabPreview>,
    previewIdQueryName: String = "previewId",
    groupName: String = AllGroupName,
): Pair<String, PreviewLabPreview>? = previewList.findBySearchParam(previewIdQueryName = previewIdQueryName)
    ?.let { groupName to it }

@Composable
public fun EmbeddedPreviewOrGallery(
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
    noSelectedContents: @Composable (Map<String, List<PreviewLabPreview>>) -> Unit = { groupedPreviews ->
        PreviewListGrid(
            groupedPreviewList = groupedPreviews,
            onPreviewClick = { group, preview -> state.select(group, preview) },
            contentPadding = PaddingValues(adaptive(12.dp, 20.dp)),
        )
    },
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
            noSelectedContents = noSelectedContents,
        )
    }
}
