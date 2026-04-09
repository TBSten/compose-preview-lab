import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import app.appPreviews
import java.io.File
import kotlin.test.Test
import me.tbsten.compose.preview.lab.gallery.PreviewLabGallery

@OptIn(ExperimentalTestApi::class)
class Playground {
    @Test
    fun play() = runComposeUiTest {
        setContent {
            PreviewLabGallery(
                previewList = appPreviews,
                featuredFileList = app.FeaturedFileList,
            )
        }

        val targetNode = onRoot()
        val outputDir = File("build/screenshot")

        targetNode.outputScreenshot(outputDir, "Playground.screenshot.png")
        targetNode.outputSemanticsTree(outputDir, "Playground.semantics.txt")
    }
}
