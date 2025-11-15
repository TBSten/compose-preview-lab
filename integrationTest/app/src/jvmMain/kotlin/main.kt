import androidx.compose.ui.window.application
import me.tbsten.compose.preview.lab.PreviewLabGalleryWindows
import me.tbsten.compose.preview.lab.openfilehandler.UrlOpenFileHandler

fun main(): Unit = application {
    PreviewLabGalleryWindows(
        previews = app.previewsAll,
        featuredFiles = app.FeaturedFiles,
        openFileHandler = UrlOpenFileHandler(
            baseUrl = "https://github.com/TBSten/compose-preview-lab/blob/main/integrationTest/",
        ),
    )
}
