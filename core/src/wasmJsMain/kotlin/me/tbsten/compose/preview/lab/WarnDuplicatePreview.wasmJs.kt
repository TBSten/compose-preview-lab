@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package me.tbsten.compose.preview.lab

private fun consoleWarn(message: String) {
    js("console.warn(message)")
}

@InternalComposePreviewLabApi
public actual fun warnDuplicatePreview(message: String) {
    consoleWarn(message)
}
