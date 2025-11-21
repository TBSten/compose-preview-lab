import androidx.compose.ui.window.application
import me.tbsten.compose.preview.lab.PreviewLabGalleryWindows
import me.tbsten.compose.preview.lab.openfilehandler.UrlOpenFileHandler
import me.tbsten.compose.preview.lab.sample.rememberPreviewLabGalleryState

fun main(): Unit = application {
    PreviewLabGalleryWindows(
        previewList = uiLib.PreviewList + helloComposePreviewLab.PreviewList,
        featuredFileList = app.FeaturedFileList,
        openFileHandler = UrlOpenFileHandler(
            baseUrl = "https://github.com/TBSten/compose-preview-lab/blob/main/integrationTest/",
        ),
        state = rememberPreviewLabGalleryState(
            initialGroupName = app.FeaturedFileList.hello_compose_preview_lab.first(),
            initialPreview = helloComposePreviewLab.PreviewList.AboutComposePreviewLab,
        ),
    )
}
