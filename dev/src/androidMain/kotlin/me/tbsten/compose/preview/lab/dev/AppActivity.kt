package me.tbsten.compose.preview.lab.dev

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import me.tbsten.compose.preview.lab.PreviewLabGallery
import me.tbsten.compose.preview.lab.PreviewLabGalleryState
import me.tbsten.compose.preview.lab.previewsForUiDebug

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PreviewLabGallery(
                state = remember { PreviewLabGalleryState() },
                previewList = previewsForUiDebug,
                modifier = Modifier
                    .systemBarsPadding()
                    .displayCutoutPadding(),
            )
        }
    }
}
