package me.tbsten.compose.preview.lab.previewlab

import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer

/**
 * JS (Browser) 向けの ZoomableContent 実装。
 * Zoomable ライブラリが JS をサポートしていないため、
 * 従来の draggable2D + graphicsLayer 実装を維持。
 */
@Composable
internal actual fun ZoomableContent(state: PreviewLabState, modifier: Modifier, content: @Composable () -> Unit,) {
    Box(
        modifier = modifier
            .draggable2D(state.contentDraggableState)
            .graphicsLayer {
                translationX = state.contentOffset.x
                translationY = state.contentOffset.y
                scaleX = state.contentScale
                scaleY = state.contentScale
                transformOrigin = TransformOrigin(0f, 0f)
            },
    ) {
        content()
    }
}
