import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabGallery
import me.tbsten.compose.preview.lab.PreviewLabGalleryState

@OptIn(ExperimentalComposePreviewLabApi::class, ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        PreviewLabGallery(
            previewList = app.PreviewList + uiLib.PreviewList + helloComposePreviewLab.PreviewList,
            featuredFileList = app.FeaturedFileList,
            state = remember { PreviewLabGalleryState() }.also { galleryState ->
                LaunchedEffect(galleryState) {
                    galleryState.select(
                        groupName = app.FeaturedFileList.`hello compose preview lab`.first(),
                        preview = helloComposePreviewLab.PreviewList.AboutComposePreviewLab,
                    )
                }
            },
        )
    }
}
