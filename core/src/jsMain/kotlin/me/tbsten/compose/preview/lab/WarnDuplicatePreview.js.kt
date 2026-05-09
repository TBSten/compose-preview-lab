package me.tbsten.compose.preview.lab

import kotlin.js.js

@InternalComposePreviewLabApi
public actual fun warnDuplicatePreview(message: String) {
    js("console.warn(message)")
}
