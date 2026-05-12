package me.tbsten.compose.preview.lab

import kotlin.js.console

@InternalComposePreviewLabApi
public actual fun warnDuplicatePreview(message: String) {
    console.warn("$WarnPrefix$message")
}
