import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import app.FeaturedFileList
import helloComposePreviewLab.PreviewList
import kotlinx.browser.document
import me.tbsten.compose.preview.lab.EmbeddedPreviewOrGallery
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.gallery.PreviewLabGalleryState
import me.tbsten.compose.preview.lab.initialSelectedPreviewFromSearchParam

@OptIn(ExperimentalComposePreviewLabApi::class, ExperimentalComposeUiApi::class, ExperimentalWasmJsInterop::class)
fun main() {
    val previewList = app.PreviewList + uiLib.PreviewList + helloComposePreviewLab.PreviewList

    ComposeViewport(document.body!!) {
        EmbeddedPreviewOrGallery(
            previewList = previewList.toList(),
            featuredFileList = app.FeaturedFileList,
            state = remember {
                PreviewLabGalleryState(
                    initialSelectedPreview =
                        initialSelectedPreviewFromSearchParam(previewList.toList())
                            ?: (
                                FeaturedFileList.hello_compose_preview_lab.first() to
                                    PreviewList.AboutComposePreviewLab
                                ),
                )
            },
        )
    }
}
