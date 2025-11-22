import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import me.tbsten.compose.preview.lab.EmbeddedPreviewOrGallery
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.sample.rememberPreviewLabGalleryState

@OptIn(ExperimentalComposePreviewLabApi::class, ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        EmbeddedPreviewOrGallery(
            previewList = app.PreviewList + uiLib.PreviewList + helloComposePreviewLab.PreviewList,
            featuredFileList = app.FeaturedFileList,
            state = rememberPreviewLabGalleryState(
                initialGroupName = app.FeaturedFileList.hello_compose_preview_lab.first(),
                initialPreview = helloComposePreviewLab.PreviewList.AboutComposePreviewLab,
            ),
        )
    }
}
