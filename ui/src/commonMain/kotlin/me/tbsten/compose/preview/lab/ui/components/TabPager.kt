package me.tbsten.compose.preview.lab.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi

/**
 * A horizontally scrolling tab strip ([PreviewLabTabs]) driving a [HorizontalPager].
 *
 * The tab list itself scrolls (the 1dp indicator is as wide as the list content, so it scrolls with the tabs) and a
 * full-width [PreviewLabHorizontalSeparator] is drawn under it instead of the list's own divider.
 *
 * @param tabs Ordered tab keys; one page per key.
 * @param title Label for a tab key.
 * @param pagerState Pager state shared with the caller; the strip follows [PagerState.targetPage].
 * @param modifier Modifier applied to the whole pager column.
 * @param content Page content for a tab key.
 */
@Composable
@UiComposePreviewLabApi
fun <T> PreviewLabTabPager(
    tabs: List<T>,
    title: (T) -> String,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    content: @Composable (tab: T) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier) {
        // An empty tab list renders no tab strip (PreviewLabTabs needs a selected tab).
        if (tabs.isNotEmpty()) {
            PreviewLabTabs(
                // targetPage: the indicator moves as soon as a tab is clicked, not when the page animation ends.
                selectedTab = tabs[pagerState.targetPage.coerceIn(0, tabs.lastIndex)],
                onSelectedTabChange = { tab ->
                    val index = tabs.indexOf(tab)
                    if (index >= 0) coroutineScope.launch { pagerState.animateScrollToPage(index) }
                },
                orderedTabs = tabs,
            ) {
                // The scroll goes on the TabList so the indicator scrolls with the tabs; the list divider would then be
                // only as wide as the tabs, hence transparent + a full-width separator below.
                PreviewLabTabList(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    dividerColor = Color.Transparent,
                ) {
                    tabs.forEach { tab ->
                        PreviewLabTab(key = tab) {
                            PreviewLabText(title(tab))
                        }
                    }
                }
            }
        }
        PreviewLabHorizontalSeparator()

        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top,
        ) { pageIndex ->
            content(tabs[pageIndex])
        }
    }
}
