package me.tbsten.compose.preview.lab.previewlab.header

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import me.tbsten.compose.preview.lab.previewlab.screenshot.LocalCaptureScreenshot
import me.tbsten.compose.preview.lab.previewlab.screenshot.rememberSaveScreenshot
import me.tbsten.compose.preview.lab.ui.components.Button
import me.tbsten.compose.preview.lab.ui.components.ButtonVariant

@Composable
internal fun Screenshot(modifier: Modifier = Modifier) {
    val captureScreenshot = LocalCaptureScreenshot.current
    val saveScreenshot = rememberSaveScreenshot()
    val scope = rememberCoroutineScope()

    Button(
        text = "Screenshot",
        variant = ButtonVariant.PrimaryOutlined,
        onClick = {
            scope.launch {
                val imageBitmap = captureScreenshot?.invoke()
                if (imageBitmap != null) {
                    saveScreenshot(imageBitmap)
                }
            }
        },
        modifier = modifier.fillMaxHeight(),
    )
}
