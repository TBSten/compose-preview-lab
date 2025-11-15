package me.tbsten.compose.preview.lab

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import me.tbsten.compose.preview.lab.openfilehandler.OpenFileHandler
import org.w3c.dom.HTMLElement

/**
 * Launches a WebAssembly application for previewing Compose components
 *
 * Creates a high-performance browser-based interface using Compose for WebAssembly.
 * Provides near-native performance for interactive component development and testing
 * in web environments. Supports the same features as the standard web version with
 * improved execution speed and memory efficiency.
 *
 * ```kotlin
 * // Basic WASM application
 * fun main() = previewLabApplication(
 *     previews = myModule.previews
 * )
 *
 * // With file handler integration
 * fun main() = previewLabApplication(
 *     previews = myModule.previews,
 *     openFileHandler = UrlOpenFileHandler("https://github.com/user/repo/blob/main"),
 *     featuredFiles = mapOf(
 *         "UI Components" to listOf("Button.kt", "TextField.kt"),
 *         "Navigation" to listOf("TopBar.kt", "Drawer.kt")
 *     )
 * )
 *
 * // Performance-optimized setup
 * fun main() {
 *     val container = document.getElementById("preview-app") as HTMLElement
 *     previewLabApplication(
 *         previews = myModule.previews,
 *         rootElement = container
 *     )
 * }
 * ```
 *
 * @param previews Collection of previews to display in the interface
 * @param featuredFiles Grouped file organization for navigation
 * @param openFileHandler Handler for opening source files (optional)
 * @param rootElement HTML element to mount the application (defaults to document.body)
 * @see PreviewLabGallery
 * @see CollectedPreview
 * @see OpenFileHandler
 */
@OptIn(ExperimentalComposeUiApi::class)
fun previewLabApplication(
    previews: List<CollectedPreview>,
    featuredFiles: Map<String, List<String>> = emptyMap(),
    openFileHandler: OpenFileHandler<out Any?>? = null,
    rootElement: HTMLElement = document.body!!,
) {
    ComposeViewport(rootElement) {
        PreviewLabGallery(
            previews = previews,
            featuredFiles = featuredFiles,
            openFileHandler = openFileHandler,
        )
    }
}
