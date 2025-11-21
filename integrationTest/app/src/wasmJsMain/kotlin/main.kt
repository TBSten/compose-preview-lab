import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabGallery
import me.tbsten.compose.preview.lab.sample.rememberPreviewLabGalleryState

@OptIn(ExperimentalComposePreviewLabApi::class, ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        PreviewLabGallery(
            previewList = app.PreviewList + uiLib.PreviewList + helloComposePreviewLab.PreviewList,
            featuredFileList = app.FeaturedFileList,
            state = rememberPreviewLabGalleryState(
                initialGroupName = app.FeaturedFileList.`hello compose preview lab`.first(),
                initialPreview = helloComposePreviewLab.PreviewList.AboutComposePreviewLab,
            ),
        )
    }
}
