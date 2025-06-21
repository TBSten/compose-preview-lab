import me.tbsten.compose.preview.lab.component.previewLabApplication
import me.tbsten.compose.preview.lab.openfilehandler.UrlOpenFileHandler

fun main(): Unit = previewLabApplication(
    previews = app.previews + uiLib.previews,
    openFileHandler = UrlOpenFileHandler(baseUrl = "https://github.com/TBSten/compose-preview-lab/blob/main/integrationTest/"),
)
