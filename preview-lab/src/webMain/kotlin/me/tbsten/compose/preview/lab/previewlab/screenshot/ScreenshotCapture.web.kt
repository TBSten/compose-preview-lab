package me.tbsten.compose.preview.lab.previewlab.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.download
import me.tbsten.compose.preview.lab.previewlab.LocalToastHostState
import me.tbsten.compose.preview.lab.ui.components.toast.ToastType

@Composable
internal actual fun rememberSaveScreenshot(): suspend (imageBitmap: ImageBitmap, fileName: String) -> Unit {
    val toastHostState = LocalToastHostState.current
    return remember(toastHostState) {
        { imageBitmap, fileName ->
            try {
                val bytes = imageBitmap.encodeToPngByteArray()
                FileKit.download(
                    bytes = bytes,
                    fileName = "$fileName.png",
                )
            } catch (e: Throwable) {
                // Surface the failure to the user via Toast so the screenshot button
                // does not silently appear to do nothing. Keep the println as a
                // developer-facing fallback (visible in the browser console).
                println("Failed to save screenshot: ${e.message}")
                toastHostState.show(
                    message = "Failed to save screenshot: ${e.message ?: e::class.simpleName ?: "unknown error"}",
                    type = ToastType.Error,
                )
            }
        }
    }
}
