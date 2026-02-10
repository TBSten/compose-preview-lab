package me.tbsten.compose.preview.lab.extension.debugger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.tbsten.compose.preview.lab.extension.debugger.DebugMenu
import me.tbsten.compose.preview.lab.extension.debugger.DebugTool
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.PreviewLabTabPager

/**
 * Composable that displays all debug tools from a [DebugMenu] using tabs layout.
 *
 * This component renders tool titles as horizontally swipeable tabs. When a tab
 * is selected, the corresponding tool's [Content][DebugTool.Content] is displayed.
 * This layout is ideal when you want to focus on one tool at a time.
 *
 * Example usage:
 * ```kotlin
 * @Composable
 * fun DebugScreen() {
 *     AppDebugMenu.TabsView()
 * }
 * ```
 *
 * @param modifier Modifier to be applied to the root layout
 *
 * @see DebugMenu.DropdownView
 * @see DebugMenu.Dialog
 */
@Composable
fun DebugMenu.TabsView(modifier: Modifier = Modifier) {
    DebugMenuThemeProvider {
        DebugMenuTabsContent(
            debugMenu = this,
            modifier = modifier,
        )
    }
}

@Composable
private fun DebugMenuTabsContent(debugMenu: DebugMenu, modifier: Modifier = Modifier) {
    val tools = debugMenu.tools

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PreviewLabTheme.colors.background),
    ) {
        if (tools.isEmpty()) {
            DebugMenuEmptyState()
        } else {
            val pagerState = rememberPagerState { tools.size }

            PreviewLabTabPager(
                tabs = tools,
                title = { it.tool.title },
                pagerState = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { entry ->
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                ) {
                    entry.tool.Content()
                }
            }
        }
    }
}
