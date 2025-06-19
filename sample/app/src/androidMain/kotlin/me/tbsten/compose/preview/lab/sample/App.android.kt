package me.tbsten.compose.preview.lab.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier
import io.github.takahirom.rin.rememberRetained
import me.tbsten.compose.preview.lab.PreviewLabRoot
import me.tbsten.compose.preview.lab.PreviewLabRootState

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PreviewLabRoot(
                state = rememberRetained { PreviewLabRootState() },
                previews = app.previews,
                modifier = Modifier
                    .systemBarsPadding()
                    .displayCutoutPadding(),
            )
        }
    }
}
