package me.tbsten.compose.preview.lab.previewlab.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.download

@Composable
internal actual fun rememberSaveScreenshot(): suspend (ImageBitmap, String) -> Unit = remember {
    { imageBitmap, fileName ->
        try {
            val bytes = imageBitmap.encodeToPngByteArray()
            FileKit.download(
                bytes = bytes,
                fileName = "$fileName.png",
            )
        } catch (e: Throwable) {
            println("Failed to save screenshot: ${e.message}")
        }
    }
}
