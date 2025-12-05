import androidx.compose.runtime.remember
import androidx.compose.ui.window.application
import app.FeaturedFileList
import me.tbsten.compose.preview.lab.PreviewLabGalleryState
import me.tbsten.compose.preview.lab.PreviewLabGalleryWindows
import me.tbsten.compose.preview.lab.openfilehandler.UrlOpenFileHandler

fun main(): Unit = application {
    PreviewLabGalleryWindows(
        previewList = app.PreviewList + uiLib.PreviewList + helloComposePreviewLab.PreviewList,
        featuredFileList = app.FeaturedFileList,
        openFileHandler = UrlOpenFileHandler(
            baseUrl = "https://github.com/TBSten/compose-preview-lab/blob/main/integrationTest/",
        ),
        state = remember {
            PreviewLabGalleryState(
                FeaturedFileList.hello_compose_preview_lab.first() to helloComposePreviewLab.PreviewList.AboutComposePreviewLab,
            )
        },
    )
}
