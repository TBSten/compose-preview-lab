import androidx.compose.ui.window.application
import me.tbsten.compose.preview.lab.PreviewLabRootWindows
import me.tbsten.compose.preview.lab.openfilehandler.UrlOpenFileHandler

fun main(): Unit = application {
    PreviewLabRootWindows(
        previews = app.previews + uiLib.previews,
        featuredFiles = app.FeaturedFiles,
        openFileHandler = UrlOpenFileHandler(
            baseUrl = "https://github.com/TBSten/compose-preview-lab/blob/main/integrationTest/",
        ),
    )
}
