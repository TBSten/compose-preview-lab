package me.tbsten.compose.preview.lab.previewlab.screenshot

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Encodes an ImageBitmap to PNG byte array.
 */
internal expect fun ImageBitmap.encodeToPngByteArray(): ByteArray
