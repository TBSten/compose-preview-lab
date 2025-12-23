package me.tbsten.compose.preview.lab.previewlab.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Callback to capture a screenshot of the ContentSection.
 * Returns null if capture fails or is not supported on the current platform.
 */
internal typealias CaptureScreenshot = suspend () -> ImageBitmap?

internal val LocalCaptureScreenshot = compositionLocalOf<CaptureScreenshot?> { null }

/**
 * Saves an ImageBitmap to a file using FileKit.
 * @param fileName Base file name without extension (e.g., "MyPreview"). Defaults to "preview-lab-screenshot".
 */
@Composable
internal expect fun rememberSaveScreenshot(): suspend (imageBitmap: ImageBitmap, fileName: String) -> Unit
