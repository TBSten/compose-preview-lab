package me.tbsten.compose.preview.lab.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.FeaturedFileList
import helloComposePreviewLab.PreviewList
import me.tbsten.compose.preview.lab.gallery.PreviewLabGallery
import me.tbsten.compose.preview.lab.gallery.PreviewLabGalleryState

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PreviewLabGallery(
                state = remember {
                    PreviewLabGalleryState(
                        initialSelectedPreview =
                            FeaturedFileList.hello_compose_preview_lab.first() to
                                PreviewList.AboutComposePreviewLab,
                    )
                },
                previewList = app.PreviewList + uiLib.PreviewList + PreviewList,
                featuredFileList = FeaturedFileList,
                modifier = Modifier
                    .systemBarsPadding()
                    .displayCutoutPadding(),
            )
        }
    }
}
