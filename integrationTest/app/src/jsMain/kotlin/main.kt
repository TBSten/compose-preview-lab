import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import me.tbsten.compose.preview.lab.EmbeddedPreviewOrGallery
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi
import me.tbsten.compose.preview.lab.extension.debugger.ui.Dialog
import me.tbsten.compose.preview.lab.gallery.PreviewLabGalleryState
import me.tbsten.compose.preview.lab.initialSelectedPreviewFromSearchParam
import me.tbsten.compose.preview.lab.renderPreviewLabPreview
import me.tbsten.compose.preview.lab.sample.debugmenu.AppDebugMenu
import org.w3c.dom.Element

@OptIn(ExperimentalJsExport::class, ExperimentalWasmJsInterop::class, UiComposePreviewLabApi::class)
@JsExport
val appPreviewList = (app.PreviewList)
    .toJsArray()

@OptIn(ExperimentalJsExport::class, ExperimentalWasmJsInterop::class, ExperimentalComposePreviewLabApi::class)
@JsExport
fun renderPreviewById(rootElement: Element, previewId: String) = renderPreviewLabPreview(
    rootElement,
    appPreviewList.find { it.id == previewId } ?: error("Invalid previewId ($previewId). Not found."),
)

@OptIn(
    ExperimentalComposePreviewLabApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalWasmJsInterop::class,
    UiComposePreviewLabApi::class,
)
fun main() {
    ComposeViewport(document.body!!) {
        Box(modifier = Modifier.fillMaxSize()) {
            EmbeddedPreviewOrGallery(
                previewList = appPreviewList.toList(),
                featuredFileList = app.FeaturedFileList,
                state = remember {
                    PreviewLabGalleryState(
                        initialSelectedPreview =
                        initialSelectedPreviewFromSearchParam(appPreviewList.toList()),
                    )
                },
            )

            AppDebugMenu.Dialog()
        }
    }
}
