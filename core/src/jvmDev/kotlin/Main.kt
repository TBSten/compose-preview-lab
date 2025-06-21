import me.tbsten.compose.preview.lab.component.previewLabApplication
import me.tbsten.compose.preview.lab.openfilehandler.OpenFileHandler
import me.tbsten.compose.preview.lab.previewsForUiDebug

fun main(): Unit = previewLabApplication(
    previews = previewsForUiDebug,
    openFileHandler = OpenFileHandler { TODO("open file: $it") },
)
