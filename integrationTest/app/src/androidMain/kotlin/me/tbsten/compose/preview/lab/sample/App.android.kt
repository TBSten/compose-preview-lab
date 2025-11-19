package me.tbsten.compose.preview.lab.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier
import me.tbsten.compose.preview.lab.PreviewLabGallery

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PreviewLabGallery(
                state = rememberPreviewLabGalleryState(
                    initialGroupName = app.FeaturedFileList.`hello compose preview lab`.first(),
                    initialPreview = helloComposePreviewLab.PreviewList.AboutComposePreviewLab,
                ),
                previewList = app.PreviewList + uiLib.PreviewList + helloComposePreviewLab.PreviewList,
                featuredFileList = app.FeaturedFileList,
                modifier = Modifier
                    .systemBarsPadding()
                    .displayCutoutPadding(),
            )
        }
    }
}
