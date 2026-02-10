import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import me.tbsten.compose.preview.lab.EmbeddedPreviewOrGallery
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi
import me.tbsten.compose.preview.lab.extension.debugger.ui.DebugMenuDrawer
import me.tbsten.compose.preview.lab.extension.debugger.ui.DebugMenuTrigger
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
        var showDebugDrawer by remember { mutableStateOf(false) }

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

            // デバッグメニュー Drawer
            DebugMenuDrawer(
                debugMenu = AppDebugMenu,
                visible = showDebugDrawer,
                onCloseRequest = { showDebugDrawer = false },
                title = "App Debug Menu",
            )
        }

        // デバッグメニュー トリガー (Shift+D で表示切り替え)
        DebugMenuTrigger(onTrigger = { showDebugDrawer = !showDebugDrawer })
    }
}
