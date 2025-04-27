import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import me.tbsten.compose.preview.lab.me.PreviewLabRoot

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val body = document.body ?: return
    ComposeViewport(body) {
        PreviewLabRoot(
            previews = app.previews(),
        )
    }
}
