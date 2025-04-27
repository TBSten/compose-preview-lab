import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import me.tbsten.compose.preview.lab.me.PreviewLabRoot
import java.awt.Dimension

fun main() = application {
    Window(
        title = "Compose Preview Lab",
        state = rememberWindowState(width = 800.dp, height = 600.dp),
        onCloseRequest = ::exitApplication,
    ) {
        window.minimumSize = Dimension(350, 600)
        PreviewLabRoot(
            previews = app.previews(),
        )
    }
}

