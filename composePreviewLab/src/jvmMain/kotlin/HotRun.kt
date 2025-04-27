import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.singleWindowApplication
import me.tbsten.compose.preview.lab.me.PreviewLab
import me.tbsten.compose.preview.lab.me.PreviewLabRoot

private val previewsForUiDebug = sequence<Pair<String, @Composable (() -> Unit)>> {
    yield("com.example.app.SimpleTextPreview" to {
        PreviewLab {
            Text("Hello SimpleTextPreview 🪩")
        }
    })

    yield("com.example.app.WithoutPreviewLab" to {
        Text("Hello SimpleTextPreview")
    })

    yield("com.example.app.Screen" to {
        PreviewLab {
            Text(
                "Preview1",
                modifier = Modifier
                    .background(Color.Gray)
                    .fillMaxSize()
            )
        }
    })
}

private fun main(): Unit = singleWindowApplication(
    title = "Compose Preview Lab UI",
) {
    PreviewLabRoot(
        previews = previewsForUiDebug,
        openFileHandler = {
            println("🪩 PreviewLabRoot.openFileHandler")
        },
    )
}
