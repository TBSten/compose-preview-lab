package me.tbsten.compose.preview.lab.previewlab.screenshot

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import java.io.ByteArrayOutputStream

// PNG is lossless; the quality parameter is ignored for PNG format.
// We keep this constant for API symmetry with other platforms.
private const val PngCompressionQuality: Int = 100

internal actual fun ImageBitmap.encodeToPngByteArray(): ByteArray {
    val bitmap = this.asAndroidBitmap()
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, PngCompressionQuality, outputStream)
    return outputStream.toByteArray()
}
