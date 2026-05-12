package me.tbsten.compose.preview.lab

import kotlin.js.console

@InternalComposePreviewLabApi
public actual fun warnDuplicatePreview(message: String) {
    // The shared [WarnPrefix] is prepended so the emitted line is byte-identical to
    // the other platforms' output. `console.warn` shows as yellow in browser DevTools
    // and is surfaced separately from `console.log` by headless test runners that
    // filter by severity.
    console.warn("$WarnPrefix$message")
}
