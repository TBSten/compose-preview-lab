@file:Suppress("ktlint:standard:filename")

import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import me.tbsten.compose.preview.lab.EmbeddedPreviewOrGallery
import me.tbsten.compose.preview.lab.gallery.PreviewLabGalleryState
import me.tbsten.compose.preview.lab.initialSelectedPreviewFromSearchParam

val appPreviewList = (sample.PreviewList + uiLib.PreviewList)

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        EmbeddedPreviewOrGallery(
            previewList = appPreviewList,
            featuredFileList = sample.FeaturedFileList,
            state = remember {
                PreviewLabGalleryState(
                    initialSelectedPreview =
                    initialSelectedPreviewFromSearchParam(appPreviewList),
                )
            },
        )
    }
}
