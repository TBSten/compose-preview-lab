package me.tbsten.compose.preview.lab.extension.debugger

import androidx.compose.runtime.Composable

/**
 * A debug tool that provides a configurable UI for debugging purposes.
 *
 * Each [DebugTool] has:
 * - [title]: A human-readable title for display in the debug menu
 * - [Content]: A Composable function that renders the tool's UI
 *
 * # Creating custom DebugTool implementations
 *
 * ```kotlin
 * class KtorInspector : DebugTool {
 *     override val title = "API Inspector"
 *
 *     val requestLogs: List<RequestLog> = mutableListOf()
 *
 *     @Composable
 *     override fun Content() {
 *         Column {
 *             Text("Request count: ${requestLogs.size}")
 *             // Custom UI for inspecting requests
 *         }
 *     }
 * }
 *
 * object AppDebugMenu : DebugMenu() {
 *     val apiClient: KtorInspector by tool { KtorInspector() }
 * }
 * ```
 */
interface DebugTool {
    /**
     * A human-readable title for this debug tool.
     *
     * This is displayed as the header in the debug menu UI.
     */
    val title: String

    /**
     * Composable function that renders the UI for this debug tool.
     *
     * This is called by [DebugMenu.View] to display the tool's content.
     * Custom implementations can provide any UI they need.
     */
    @Composable
    fun Content()
}
