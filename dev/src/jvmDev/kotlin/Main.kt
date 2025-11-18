import androidx.compose.ui.window.application
import me.tbsten.compose.preview.lab.PreviewLabGalleryWindows
import me.tbsten.compose.preview.lab.featuredFilesForDebug
import me.tbsten.compose.preview.lab.openfilehandler.OpenFileHandler
import me.tbsten.compose.preview.lab.previewsForUiDebug

fun main(): Unit = application {
    PreviewLabGalleryWindows(
        previewList = previewsForUiDebug,
        featuredFileList = featuredFilesForDebug,
        openFileHandler = OpenFileHandler { TODO("open file: $it") },
    )
}
