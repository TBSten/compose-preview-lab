package me.tbsten.compose.preview.lab.previewlab.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.write

@Composable
internal actual fun rememberSaveScreenshot(): suspend (ImageBitmap) -> Unit = remember {
    { imageBitmap ->
        try {
            val bytes = imageBitmap.encodeToPngByteArray()
            val file: PlatformFile? = FileKit.openFileSaver(
                suggestedName = "preview-lab-screenshot",
                extension = "png",
            )
            file?.write(bytes)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
