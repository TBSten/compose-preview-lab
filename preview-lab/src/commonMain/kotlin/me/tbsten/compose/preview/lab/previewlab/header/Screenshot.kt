package me.tbsten.compose.preview.lab.previewlab.header

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import me.tbsten.compose.preview.lab.LocalPreviewLabPreview
import me.tbsten.compose.preview.lab.previewlab.LocalToastHostState
import me.tbsten.compose.preview.lab.previewlab.screenshot.LocalCaptureScreenshot
import me.tbsten.compose.preview.lab.previewlab.screenshot.rememberSaveScreenshot
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.PreviewLabIcon
import me.tbsten.compose.preview.lab.ui.components.PreviewLabText
import me.tbsten.compose.preview.lab.ui.components.toast.ToastType
import me.tbsten.compose.preview.lab.ui.generated.resources.PreviewLabUiRes
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_screenshot_frame
import org.jetbrains.compose.resources.painterResource

private const val DefaultScreenshotFileName = "preview-lab-screenshot"
private const val UnknownErrorMessage = "unknown error"

@Composable
internal fun Screenshot(modifier: Modifier = Modifier) {
    val captureScreenshot = LocalCaptureScreenshot.current
    val saveScreenshot = rememberSaveScreenshot()
    val scope = rememberCoroutineScope()
    val displayName = LocalPreviewLabPreview.current?.displayName
    val toastHostState = LocalToastHostState.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        modifier = modifier
            .semantics(mergeDescendants = true) { }
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                scope.launch {
                    val imageBitmap = captureScreenshot?.invoke() ?: return@launch
                    val fileName = displayName ?: DefaultScreenshotFileName
                    try {
                        saveScreenshot(imageBitmap, fileName)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        val detail = e.message ?: e::class.simpleName ?: UnknownErrorMessage
                        toastHostState.show(
                            message = "Failed to save screenshot: $detail",
                            type = ToastType.Error,
                        )
                    }
                }
            }
            .padding(8.dp),
    ) {
        PreviewLabIcon(
            painter = painterResource(PreviewLabUiRes.drawable.icon_screenshot_frame),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )

        PreviewLabText(
            text = "Screenshot",
            style = PreviewLabTheme.typography.label3,
            textAlign = TextAlign.Center,
        )
    }
}
