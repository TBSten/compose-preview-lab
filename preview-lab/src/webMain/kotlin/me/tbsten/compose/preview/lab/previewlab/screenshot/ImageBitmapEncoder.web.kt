package me.tbsten.compose.preview.lab.previewlab.screenshot

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

// PNG is lossless; the quality parameter is typically ignored for PNG format in Skia.
private const val PngCompressionQuality: Int = 100

internal actual fun ImageBitmap.encodeToPngByteArray(): ByteArray {
    val data = Image
        .makeFromBitmap(this.asSkiaBitmap())
        .encodeToData(EncodedImageFormat.PNG, PngCompressionQuality)

    if (data == null) {
        println("ImageBitmap.encodeToPngByteArray: encodeToData returned null; returning empty PNG byte array.")
        return byteArrayOf()
    }

    return data.bytes
}
