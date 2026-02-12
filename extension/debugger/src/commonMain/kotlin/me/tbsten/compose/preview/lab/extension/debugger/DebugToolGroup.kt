package me.tbsten.compose.preview.lab.extension.debugger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ui.components.PreviewLabDivider

abstract class DebugToolGroup(override val title: String) :
    DebugTool,
    DebugToolRegistry {
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

    @Composable
    override fun Content(context: DebugTool.ContentContext) {
        val filteredTools = tools.filter { context.isHit(it.name) }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(12.dp),
        ) {
            filteredTools.forEachIndexed { index, toolEntry ->
                toolEntry.tool.Content(context)

                if (index != tools.lastIndex) PreviewLabDivider(modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
    }
}
