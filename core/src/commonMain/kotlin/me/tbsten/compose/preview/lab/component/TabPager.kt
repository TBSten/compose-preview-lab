package me.tbsten.compose.preview.lab.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
internal fun <T> TabPager(
    tabs: List<T>,
    title: (T) -> String,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    content: @Composable (tab: T) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier) {
        pagerState.currentPage
        // TODO
//        ScrollableTabRow(
//            selectedTabIndex = pagerState.currentPage,
//            edgePadding = 0.dp,
//            modifier = Modifier.fillMaxWidth(),
//        ) {
//            tabs.forEachIndexed { index, tab ->
//                Tab(
//                    selected = pagerState.currentPage == index,
//                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
//                    text = { Text(title(tab)) },
//                )
//            }
//        }

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
