package me.tbsten.compose.preview.lab.previewlab.screenshot

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

internal actual fun ImageBitmap.encodeToPngByteArray(): ByteArray = Image.makeFromBitmap(this.asSkiaBitmap())
    .encodeToData(EncodedImageFormat.PNG, 100)
    ?.bytes
    ?: byteArrayOf()
