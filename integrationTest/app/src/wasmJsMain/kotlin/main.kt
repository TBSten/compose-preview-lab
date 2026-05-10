import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import app.appPreviews
import kotlinx.browser.document
import me.tbsten.compose.preview.lab.EmbeddedPreviewOrGallery
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.gallery.PreviewLabGalleryState
import me.tbsten.compose.preview.lab.initialSelectedPreviewFromSearchParam

@OptIn(ExperimentalComposePreviewLabApi::class, ExperimentalComposeUiApi::class)
fun main() {
    val previewList = appPreviews.toList()

    ComposeViewport(document.body!!) {
        EmbeddedPreviewOrGallery(
            previewList = previewList,
            featuredFileList = app.FeaturedFileList,
            state = remember {
                PreviewLabGalleryState(
                    initialSelectedPreview =
                    initialSelectedPreviewFromSearchParam(previewList),
                )
            },
        )
    }
}
