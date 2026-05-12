package me.tbsten.compose.preview.lab.previewlab.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.CancellationException
import me.tbsten.compose.preview.lab.previewlab.LocalToastHostState
import me.tbsten.compose.preview.lab.ui.components.toast.ToastType

private const val UnknownErrorMessage = "unknown error"

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
            } catch (e: CancellationException) {
                // Coroutine cancellation must propagate cooperatively.
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                val detail = e.message ?: e::class.simpleName ?: UnknownErrorMessage
                toastHostState.show(
                    message = "Failed to save screenshot: $detail",
                    type = ToastType.Error,
                )
            }
        }
    }
}
