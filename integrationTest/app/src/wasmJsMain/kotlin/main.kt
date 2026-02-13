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
import me.tbsten.compose.preview.lab.sample.debugmenu.AppDebugMenu

@OptIn(
    ExperimentalComposePreviewLabApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalWasmJsInterop::class,
    UiComposePreviewLabApi::class,
)
fun main() {
    val previewList = app.PreviewList

    ComposeViewport(document.body!!) {
        Box(modifier = Modifier.fillMaxSize()) {
            EmbeddedPreviewOrGallery(
                previewList = previewList.toList(),
                featuredFileList = app.FeaturedFileList,
                state = remember {
                    PreviewLabGalleryState(
                        initialSelectedPreview =
                        initialSelectedPreviewFromSearchParam(previewList.toList()),
                    )
                },
            )

            AppDebugMenu.Dialog()
        }
    }
}
