package me.tbsten.compose.preview.lab.extension.debugger

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import me.tbsten.compose.preview.lab.extension.debugger.ui.TabsView

/**
 * Base class for creating a debug menu with configurable debug tools.
 *
 * DebugMenu is a container that holds multiple [DebugTool]s. Each DebugTool
 * corresponds to a tab in the debug menu UI, providing specific debugging functionality.
 *
 * ```
 * DebugMenu
 * ├── DebugTool-1 (corresponds to each tab in the debug menu)
 * ├── DebugTool-2
 * └── ...
 * ```
 *
 * # Basic Usage
 *
 * Define your application's debug menu by extending this class:
 *
 * ```kotlin
 * object AppDebugMenu : DebugMenu() {
 *     val logger = tool { SimpleLogger() }
 * }
 * ```
 *
 * # Displaying the Debug Menu
 *
 * Use [Dialog] for automatic platform-specific trigger support (shake on mobile,
 * keyboard shortcut on web), or use [TabsView]/[DropdownView] for direct embedding.
 *
 * ```kotlin
 * @Composable
 * fun App() {
 *     MainContent()
 *
 *     // Debug menu dialog (shown via shake or Shift+D)
 *     AppDebugMenu.Dialog()
 * }
 * ```
 *
 * @see DebugTool
 * @see DebugMenu.Dialog
 * @see DebugMenu.TabsView
 * @see DebugMenu.DropdownView
 */
abstract class DebugMenu : DebugToolRegistry {

    private val _tools = mutableStateListOf<DebugToolEntry>()

    /**
     * List of all registered debug tools.
     */
    override val tools: List<DebugToolEntry> by derivedStateOf { _tools }

    /**
     * @see DebugToolRegistry.tool
     */
    override fun <D : DebugTool> tool(builder: () -> D): D {
        val tool = builder()
        val entry = DebugToolEntry(
            name = tool.title,
            tool = tool,
        )
        _tools.add(entry)
        return tool
    }
}

/**
 * Entry representing a registered debug tool.
 *
 * @param name The name of the tool (used for identification)
 * @param tool The debug tool instance
 */
data class DebugToolEntry(val name: String, val tool: DebugTool)
