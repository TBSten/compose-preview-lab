package me.tbsten.compose.preview.lab.extension.debugger

interface DebugToolRegistry {
    /**
     * List of all registered debug tools.
     */
    val tools: List<DebugToolEntry>

    /**
     * Registers a [DebugTool] and returns it.
     *
     * The tool is created immediately and registered with this [DebugMenu].
     * The concrete type of the debug tool is preserved, allowing access to
     * custom properties and methods.
     *
     * # Usage:
     * ```kotlin
     * val customTool = tool { MyCustomDebugTool() }
     * // Access custom properties:
     * customTool.someCustomProperty
     * ```
     *
     * @param D The concrete type of the debug tool (preserves specific subtype)
     * @param builder A factory function that creates the [DebugTool]
     * @return The created [DebugTool] instance
     */
    fun <D : DebugTool> tool(builder: () -> D): D
}
