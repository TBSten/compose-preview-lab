import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import me.tbsten.compose.preview.lab.EmbeddedPreviewOrGallery
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.renderPreviewLabPreview
import me.tbsten.compose.preview.lab.sample.rememberPreviewLabGalleryState
import org.w3c.dom.Element

@OptIn(ExperimentalJsExport::class, ExperimentalWasmJsInterop::class)
@JsExport
val appPreviewList = (app.PreviewList + uiLib.PreviewList + helloComposePreviewLab.PreviewList)
    .toJsArray()

@OptIn(ExperimentalJsExport::class, ExperimentalWasmJsInterop::class, ExperimentalComposePreviewLabApi::class)
@JsExport
fun renderPreviewById(rootElement: Element, previewId: String) = renderPreviewLabPreview(
    rootElement,
    appPreviewList.find { it.id == previewId } ?: error("Invalid previewId ($previewId). Not found."),
)

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
