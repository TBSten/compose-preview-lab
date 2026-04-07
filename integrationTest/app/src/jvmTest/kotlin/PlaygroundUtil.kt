import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.printToString
import java.io.File
import javax.imageio.ImageIO

fun SemanticsNodeInteraction.outputScreenshot(outputDir: File, fileName: String) {
    val image =
        this
            .captureToImage()
            .toAwtImage()
    ImageIO.write(
        image,
        "png",
        File(outputDir, fileName)
            .also { it.parentFile.mkdirs() }
            .also { println("output screenshot to ${it.absolutePath}") },
    )
}

fun SemanticsNodeInteraction.outputSemanticsTree(outputDir: File, fileName: String, maxDepth: Int = Int.MAX_VALUE) {
    val semanticsTreeText = this.printToString(maxDepth = maxDepth)

    File(outputDir, fileName)
        .also { it.parentFile.mkdirs() }
        .also { println("output semantics tree to ${it.absolutePath}") }
        .writeText(text = semanticsTreeText)
}
