import androidx.compose.ui.ExperimentalComposeUiApi
import me.tbsten.compose.preview.lab.featuredFilesForDebug
import me.tbsten.compose.preview.lab.previewLabApplication
import me.tbsten.compose.preview.lab.previewsForUiDebug

@OptIn(ExperimentalComposeUiApi::class)
fun main() = previewLabApplication(
    previews = app.previewsAll + previewsForUiDebug,
    featuredFiles = app.FeaturedFiles + featuredFilesForDebug,
)
