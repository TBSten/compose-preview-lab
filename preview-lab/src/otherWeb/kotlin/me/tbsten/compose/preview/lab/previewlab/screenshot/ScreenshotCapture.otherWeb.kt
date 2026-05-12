package me.tbsten.compose.preview.lab.previewlab.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.write
import me.tbsten.compose.preview.lab.previewlab.LocalToastHostState
import me.tbsten.compose.preview.lab.ui.components.toast.ToastType

@Composable
internal actual fun rememberSaveScreenshot(): suspend (imageBitmap: ImageBitmap, fileName: String) -> Unit {
    val toastHostState = LocalToastHostState.current
    return remember(toastHostState) {
        { imageBitmap, fileName ->
            try {
                val bytes = imageBitmap.encodeToPngByteArray()
                val file: PlatformFile? = FileKit.openFileSaver(
                    suggestedName = fileName,
                    extension = "png",
                )
                file?.write(bytes)
            } catch (e: Exception) {
                // Surface the failure to the user via Toast so the screenshot button
                // does not silently appear to do nothing. Keep printStackTrace as a
                // developer-facing fallback (e.g. visible in console / logcat).
                e.printStackTrace()
                toastHostState.show(
                    message = "Failed to save screenshot: ${e.message ?: e::class.simpleName ?: "unknown error"}",
                    type = ToastType.Error,
                )
            }
        }
    }
}
