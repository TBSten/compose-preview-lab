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
 *     previewList = myModule.PreviewList
 * )
 *
 * // With file handler integration
 * fun main() = previewLabApplication(
 *     previewList = myModule.PreviewList,
 *     openFileHandler = UrlOpenFileHandler("https://github.com/user/repo/blob/main"),
 *     featuredFileList = mapOf(
 *         "UI Components" to listOf("Button.kt", "TextField.kt"),
 *         "Navigation" to listOf("TopBar.kt", "Drawer.kt")
 *     )
 * )
 *
 * // Performance-optimized setup
 * fun main() {
 *     val container = document.getElementById("preview-app") as HTMLElement
 *     previewLabApplication(
 *         previewList = myModule.PreviewList,
 *         rootElement = container
 *     )
 * }
 * ```
 *
 * @param previewList Collection of previews to display in the interface
 * @param featuredFileList Grouped file organization for navigation
 * @param openFileHandler Handler for opening source files (optional)
 * @param state PreviewLabGalleryState for managing gallery state
 * @param rootElement HTML element to mount the application (defaults to document.body)
 * @see PreviewLabGallery
 * @see CollectedPreview
 * @see OpenFileHandler
 */
@OptIn(ExperimentalComposeUiApi::class)
fun previewLabApplication(
    previewList: List<PreviewLabPreview>,
    featuredFileList: Map<String, List<String>> = emptyMap(),
    openFileHandler: OpenFileHandler<out Any?>? = null,
    state: PreviewLabGalleryState = PreviewLabGalleryState(),
    rootElement: HTMLElement = document.body!!,
) {
    ComposeViewport(rootElement) {
        PreviewLabGallery(
            previewList = previewList,
            featuredFileList = featuredFileList,
            openFileHandler = openFileHandler,
            state = state,
        )
    }
}
