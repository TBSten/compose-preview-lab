import androidx.compose.ui.window.application
import me.tbsten.compose.preview.lab.PreviewLabGalleryWindows
import me.tbsten.compose.preview.lab.openfilehandler.UrlOpenFileHandler

fun main(): Unit = application {
    PreviewLabGalleryWindows(
        previewList = app.PreviewAllList,
        featuredFileList = app.FeaturedFileList,
        openFileHandler = UrlOpenFileHandler(
            baseUrl = "https://github.com/TBSten/compose-preview-lab/blob/main/integrationTest/",
        ),
    )
}
