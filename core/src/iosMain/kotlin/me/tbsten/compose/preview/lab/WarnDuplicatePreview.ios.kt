@file:OptIn(ExperimentalForeignApi::class)

package me.tbsten.compose.preview.lab

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSLog

@InternalComposePreviewLabApi
public actual fun warnDuplicatePreview(message: String) {
    NSLog("%@%@", WarnPrefix as Any, message as Any)
}
