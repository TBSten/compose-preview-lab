import androidx.compose.runtime.remember
import androidx.compose.ui.window.application
import me.tbsten.compose.preview.lab.PreviewLabGalleryWindows
import me.tbsten.compose.preview.lab.gallery.PreviewLabGalleryState
import me.tbsten.compose.preview.lab.previewlab.openfilehandler.UrlOpenFileHandler

fun main(): Unit = application {
    PreviewLabGalleryWindows(
        previewList = app.PreviewList,
        featuredFileList = app.FeaturedFileList,
        openFileHandler = UrlOpenFileHandler(
            baseUrl = "https://github.com/TBSten/compose-preview-lab/blob/main/integrationTest/",
        ),
        state = remember {
            PreviewLabGalleryState()
        },
    )
}
