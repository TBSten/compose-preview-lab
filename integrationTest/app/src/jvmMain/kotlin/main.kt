import androidx.compose.ui.window.application
import me.tbsten.compose.preview.lab.PreviewLabRootWindows
import me.tbsten.compose.preview.lab.featuredFilesForDebug
import me.tbsten.compose.preview.lab.openfilehandler.UrlOpenFileHandler
import me.tbsten.compose.preview.lab.previewsForUiDebug

fun main(): Unit = application {
    PreviewLabRootWindows(
        previews = app.previewsAll + previewsForUiDebug,
        featuredFiles = app.FeaturedFiles + featuredFilesForDebug,
        openFileHandler = UrlOpenFileHandler(
            baseUrl = "https://github.com/TBSten/compose-preview-lab/blob/main/integrationTest/",
        ),
    )
}
