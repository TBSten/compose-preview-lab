import me.tbsten.compose.preview.lab.featuredFilesForDebug
import me.tbsten.compose.preview.lab.openfilehandler.OpenFileHandler
import me.tbsten.compose.preview.lab.previewLabApplication
import me.tbsten.compose.preview.lab.previewsForUiDebug

fun main(): Unit = previewLabApplication(
    previews = previewsForUiDebug,
    featuredFiles = featuredFilesForDebug,
    openFileHandler = OpenFileHandler { TODO("open file: $it") },
)
