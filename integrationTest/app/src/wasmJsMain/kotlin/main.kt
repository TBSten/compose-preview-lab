import androidx.compose.ui.ExperimentalComposeUiApi
import me.tbsten.compose.preview.lab.previewLabApplication

@OptIn(ExperimentalComposeUiApi::class)
fun main() = previewLabApplication(
    previews = app.PreviewAllList,
    featuredFiles = app.FeaturedFileList,
)
