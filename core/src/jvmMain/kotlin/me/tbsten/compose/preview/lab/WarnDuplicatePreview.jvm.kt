package me.tbsten.compose.preview.lab

@InternalComposePreviewLabApi
public actual fun warnDuplicatePreview(message: String) {
    // `System.err` (vs `println` / `System.out`) keeps the warning off stdout — which
    // build/test runners parse for structured output — and onto the dedicated error
    // stream that IDEs and CI surfaces highlight separately. The shared [WarnPrefix]
    // is prepended so log-collector filters configured against the literal prefix
    // match identical lines across every platform.
    System.err.println("$WarnPrefix$message")
}
