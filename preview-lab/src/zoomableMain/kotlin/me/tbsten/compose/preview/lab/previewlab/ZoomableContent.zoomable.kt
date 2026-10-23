package me.tbsten.compose.preview.lab.previewlab

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@Composable
internal actual fun ZoomableContent(state: PreviewLabState, modifier: Modifier, content: @Composable () -> Unit,) {
    val zoomState = rememberZoomState()

    // ZoomState → PreviewLabState 同期
    LaunchedEffect(zoomState, state) {
        snapshotFlow { Triple(zoomState.scale, zoomState.offsetX, zoomState.offsetY) }
            .collect { (scale, offsetX, offsetY) ->
                state.contentScale = scale
                state.contentOffset = Offset(offsetX, offsetY)
            }
    }

    // PreviewLabState → ZoomState 同期（ヘッダーボタン操作時）
    LaunchedEffect(zoomState, state) {
        snapshotFlow { state.contentScale }
            .distinctUntilChanged()
            .drop(1)
            .collect { scale ->
                if (zoomState.scale != scale) {
                    zoomState.changeScale(scale, Offset.Zero)
                }
            }
    }

    // PreviewLabState.contentOffset のリセット対応
    LaunchedEffect(zoomState, state) {
        snapshotFlow { state.contentOffset }
            .distinctUntilChanged()
            .drop(1)
            .collect { offset ->
                if (offset == Offset.Zero && (zoomState.offsetX != 0f || zoomState.offsetY != 0f)) {
                    zoomState.reset()
                }
            }
    }

    Box(
        modifier = modifier.zoomable(zoomState),
    ) {
        content()
    }
}
