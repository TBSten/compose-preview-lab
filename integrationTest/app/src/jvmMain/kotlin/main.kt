import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.window.application
import me.tbsten.compose.preview.lab.PreviewLabGalleryState
import me.tbsten.compose.preview.lab.PreviewLabGalleryWindows
import me.tbsten.compose.preview.lab.openfilehandler.UrlOpenFileHandler

fun main(): Unit = application {
    val galleryState = remember { PreviewLabGalleryState() }.also { galleryState ->
        LaunchedEffect(galleryState) {
            galleryState.select(
                groupName = app.FeaturedFiles.`hello compose preview lab`.first(),
                preview = helloComposePreviewLab.Previews.AboutComposePreviewLab,
            )
        }
    }

    PreviewLabGalleryWindows(
        previews = app.PreviewsAll,
        featuredFiles = app.FeaturedFiles,
        openFileHandler = UrlOpenFileHandler(
            baseUrl = "https://github.com/TBSten/compose-preview-lab/blob/main/integrationTest/",
        ),
        state = galleryState,
    )
}
