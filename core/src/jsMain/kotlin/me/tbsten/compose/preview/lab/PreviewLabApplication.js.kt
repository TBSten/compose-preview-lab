package me.tbsten.compose.preview.lab

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import me.tbsten.compose.preview.lab.openfilehandler.OpenFileHandler
import org.w3c.dom.HTMLElement

/**
 * Launches a web application for previewing Compose components
 * 
 * Creates a browser-based interface for interactive component development and testing.
 * Utilizes Compose for Web to render the PreviewLab interface in a web viewport.
 * Supports file opening handlers and featured file organization for web-based
 * development workflows.
 * 
 * ```kotlin
 * // Basic web application
 * fun main() = previewLabApplication(
 *     previews = myModule.previews
 * )
 * 
 * // With file handler and grouping
 * fun main() = previewLabApplication(
 *     previews = myModule.previews,
 *     openFileHandler = UrlOpenFileHandler("https://github.com/user/repo/blob/main"),
 *     featuredFiles = mapOf(
 *         "Components" to listOf("Button.kt", "Card.kt"),
 *         "Screens" to listOf("Home.kt", "Profile.kt")
 *     )
 * )
 * 
 * // Custom root element
 * fun main() {
 *     val customContainer = document.getElementById("preview-container") as HTMLElement
 *     previewLabApplication(
 *         previews = myModule.previews,
 *         rootElement = customContainer
 *     )
 * }
 * ```
 * 
 * @param previews Collection of previews to display in the interface
 * @param featuredFiles Grouped file organization for navigation
 * @param openFileHandler Handler for opening source files (optional)
 * @param rootElement HTML element to mount the application (defaults to document.body)
 * @see PreviewLabRoot
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
        PreviewLabRoot(
            previews = previews,
            featuredFiles = featuredFiles,
            openFileHandler = openFileHandler,
        )
    }
}
