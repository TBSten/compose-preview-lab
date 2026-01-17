package me.tbsten.compose.preview.lab.ui.util

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi

/**
 * Automatically scrolls to the top of a LazyList when the key changes,
 * but only if the list was already at the top position.
 *
 * @param listState The LazyListState to control
 * @param key A key that triggers the scroll when changed
 */
@InternalComposePreviewLabApi
@Composable
fun AutoScrollToTopEffect(listState: LazyListState, key: Any) {
    LaunchedEffect(key) {
        val wasAtTop = listState.firstVisibleItemIndex == 0 &&
            listState.firstVisibleItemScrollOffset == 0
        if (wasAtTop) {
            listState.animateScrollToItem(0)
        }
    }
}
