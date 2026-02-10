package me.tbsten.compose.preview.lab.extension.debugger

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import me.tbsten.compose.preview.lab.extension.debugger.ui.TabsView

/**
 * Base class for creating a debug menu with configurable debug tools.
 *
 * Extend this class to define your application's debug menu with [DebugTool]s
 * that can be configured at runtime through a UI.
 *
 * # Custom DebugTool implementations
 *
 * You can create and register custom [DebugTool] implementations that provide
 * additional properties and methods:
 *
 * ```kotlin
 * class KtorInspector : DebugTool {
 *     override val title = "API Inspector"
 *
 *     val requestLogs: List<RequestLog> = mutableListOf()
 *
 *     @Composable
 *     override fun Content() {
 *         Text("Logs: ${requestLogs.size}")
 *     }
 * }
 *
 * object AppDebugMenu : DebugMenu() {
 *     val apiClient = tool { KtorInspector() }
 * }
 *
 * // Access custom properties:
 * val logs = AppDebugMenu.apiClient.requestLogs
 * ```
 *
 * @see DebugTool
 * @see DebugMenu.TabsView
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
