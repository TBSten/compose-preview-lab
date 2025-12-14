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
import me.tbsten.compose.preview.lab.renderPreviewLabPreview
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

@OptIn(ExperimentalComposePreviewLabApi::class, ExperimentalComposeUiApi::class, ExperimentalWasmJsInterop::class)
fun main() {
    ComposeViewport(document.body!!) {
        EmbeddedPreviewOrGallery(
            previewList = appPreviewList.toList(),
            featuredFileList = app.FeaturedFileList,
            state = remember {
                PreviewLabGalleryState(
                    initialSelectedPreview =
                        initialSelectedPreviewFromSearchParam(appPreviewList.toList())
                            ?: (
                                FeaturedFileList.hello_compose_preview_lab.first() to
                                    PreviewList.AboutComposePreviewLab
                                ),
                )
            },
        )
    }
}
