package me.tbsten.compose.preview.lab

import androidx.compose.foundation.gestures.Draggable2DState
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.DpOffset

class PreviewLabState {
    var contentRootOffsetInAppRoot by mutableStateOf<DpOffset?>(null)
    var contentOffset by mutableStateOf(Offset.Zero)
    val contentDraggableState = Draggable2DState { contentOffset += it }
    var contentScale by mutableStateOf(1f)

    var selectedTabIndex by mutableIntStateOf(0)

    val scope: PreviewLabScope = PreviewLabScope()
}

internal fun Modifier.previewLabContent(state: PreviewLabState) =
    then(
        Modifier
            .draggable2D(state.contentDraggableState)
            .graphicsLayer {
                translationX = state.contentOffset.x
                translationY = state.contentOffset.y
                transformOrigin = TransformOrigin(0f, 0f)
            }
    )
