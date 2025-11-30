import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.LocalDefaultIsHeaderShow
import me.tbsten.compose.preview.lab.PreviewLabGallery
import me.tbsten.compose.preview.lab.findEmbedded
import me.tbsten.compose.preview.lab.sample.rememberPreviewLabGalleryState

@OptIn(ExperimentalComposePreviewLabApi::class, ExperimentalComposeUiApi::class, ExperimentalWasmJsInterop::class)
fun main() {
    val previewList = app.PreviewList + uiLib.PreviewList + helloComposePreviewLab.PreviewList

    ComposeViewport(document.body!!) {
        previewList.findEmbedded()?.let { selectedPreview ->
            CompositionLocalProvider(
                LocalDefaultIsHeaderShow provides false,
            ) {
                selectedPreview.content()
            }
        } ?: run {
            PreviewLabGallery(
                previewList = previewList,
                featuredFileList = app.FeaturedFileList,
                state = rememberPreviewLabGalleryState(
                    initialGroupName = app.FeaturedFileList.hello_compose_preview_lab.first(),
                    initialPreview = helloComposePreviewLab.PreviewList.AboutComposePreviewLab,
                ),
            )
        }
    }
}
