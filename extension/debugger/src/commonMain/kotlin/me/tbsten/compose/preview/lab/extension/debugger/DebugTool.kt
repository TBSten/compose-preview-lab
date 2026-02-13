package me.tbsten.compose.preview.lab.extension.debugger

import androidx.compose.runtime.Composable

/**
 * A debug tool that provides a configurable UI for debugging purposes.
 *
 * Each [DebugTool] corresponds to a tab in the [DebugMenu] UI, providing:
 * - [title]: A human-readable title displayed as the tab name
 * - [Content]: A Composable function that renders the tool's UI when the tab is selected
 *
 * # Creating Custom DebugTool
 *
 * You can create custom DebugTool implementations by implementing this interface:
 *
 * ```kotlin
 * class MyLogger(
 *     override val title: String = "Logger",
 * ) : DebugTool {
 *     private var logs by mutableStateListOf<String>()
 *
 *     @Composable
 *     override fun Content() {
 *         LogList(logList = logs)
 *     }
 *
 *     fun log(message: String) {
 *         logs.add(message)
 *     }
 * }
 *
 * object AppDebugMenu : DebugMenu() {
 *     val logger = tool { MyLogger() }
 * }
 * ```
 *
 * You can call methods on the tool to update internal state and reflect changes in the debug menu:
 *
 * ```kotlin
 * AppDebugMenu.logger.log("Hello!")
 * ```
 *
 * @see DebugMenu
 * @see SimpleLogger
 * @see basicFunctionDebugBehavior
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
     * This is called by [DebugMenu.TabsView] or [DebugMenu.DropdownView] to display
     * the tool's content when selected.
     */
    @Composable
    fun Content(context: ContentContext)

    data class ContentContext(val searchText: String) {
        fun isHit(text: String) = searchText.trim()
            .let {
                if (it.isBlank()) {
                    true
                } else {
                    it.split(Regex("""\s+"""))
                        .any { text.contains(it) }
                }
            }
    }
}
