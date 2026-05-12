package me.tbsten.compose.preview.lab

@InternalComposePreviewLabApi
public actual fun warnDuplicatePreview(message: String) {
    System.err.println("$WarnPrefix$message")
}
