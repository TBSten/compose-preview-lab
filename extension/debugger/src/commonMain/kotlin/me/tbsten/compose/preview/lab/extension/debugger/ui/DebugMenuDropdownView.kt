package me.tbsten.compose.preview.lab.extension.debugger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.extension.debugger.DebugMenu
import me.tbsten.compose.preview.lab.extension.debugger.DebugTool
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.PreviewLabSelectButton

/**
 * Composable that displays all debug tools from a [DebugMenu] using dropdown layout.
 *
 * This component renders a dropdown selector at the top, with the selected tool's
 * content displayed below. This layout is ideal when you need a compact display.
 *
 * Example usage:
 * ```kotlin
 * @Composable
 * fun DebugScreen() {
 *     AppDebugMenu.DropdownView()
 * }
 * ```
 *
 * @param modifier Modifier to be applied to the root layout
 *
 * @see DebugMenu.TabsView
 * @see DebugMenu.Dialog
 */
@Composable
fun DebugMenu.DropdownView(modifier: Modifier = Modifier) {
    DebugMenuThemeProvider {
        DebugMenuDropdownContent(
            debugMenu = this,
            modifier = modifier,
        )
    }
}

@Composable
private fun DebugMenuDropdownContent(debugMenu: DebugMenu, modifier: Modifier = Modifier) {
    val tools = debugMenu.tools

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PreviewLabTheme.colors.background),
    ) {
        if (tools.isEmpty()) {
            DebugMenuEmptyState()
        } else {
            var selectedIndex by remember { mutableIntStateOf(0) }

            Column(modifier = Modifier.fillMaxSize()) {
                PreviewLabSelectButton(
                    choices = tools,
                    currentIndex = selectedIndex,
                    onSelect = { selectedIndex = it },
                    title = { "Tool: " + it.tool.title },
                    modifier = Modifier.padding(8.dp),
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    tools[selectedIndex].tool.Content(
                        context = DebugTool.ContentContext(
                            searchText = "", // TODO
                        ),
                    )
                }
            }
        }
    }
}
