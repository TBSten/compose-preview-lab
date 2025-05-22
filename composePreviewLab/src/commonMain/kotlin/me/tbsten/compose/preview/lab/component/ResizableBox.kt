package me.tbsten.compose.preview.lab.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.util.thenIf
import me.tbsten.compose.preview.lab.util.thenIfNotNull

@Composable
internal fun ResizableBox(
    maxWidth: Dp?,
    maxHeight: Dp?,
    modifier: Modifier = Modifier,
    contentScale: Float = 1f,
    resizeState: ResizeState = rememberResizeState(maxWidth, maxHeight),
    content: @Composable () -> Unit,
) {
    val contentBorderWidth = 8.dp
    val contentBorderColor = MaterialTheme.colorScheme.outline

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = contentScale
                scaleY = contentScale
                transformOrigin = TransformOrigin(0f, 0f)
            }
            .width(IntrinsicSize.Max)
            .height(IntrinsicSize.Max),
    ) {
        // contents
        Box(
            modifier = Modifier
                .border(
                    width = contentBorderWidth,
                    brush = Brush.linearGradient(
                        0f to contentBorderColor.copy(alpha = 0.15f),
                        1f to contentBorderColor.copy(alpha = 0.25f),
                    ),
                    shape = RectangleShape,
                )
                .padding(contentBorderWidth)
                .fillMaxSize()
                .resizable(resizeState)
                .clip(RectangleShape)
        ) {
            content()
        }

        // right handle
        Box(
            modifier = Modifier
                .resizableHandle(resizeState, ResizableHandleDirection.X)
                .fillMaxHeight()
                .width(contentBorderWidth)
                .align(Alignment.BottomEnd)
                .offset(x = -contentBorderWidth)
        )

        // bottom handle
        Box(
            modifier = Modifier
                .resizableHandle(resizeState, ResizableHandleDirection.Y)
                .fillMaxWidth()
                .height(contentBorderWidth)
                .align(Alignment.BottomEnd)
                .offset(x = -contentBorderWidth)
        )

        // right bottom handle
        Box(
            modifier = Modifier
                .resizableHandle(resizeState, ResizableHandleDirection.Both)
                .background(Color.White)
                .border(1.5.dp, contentBorderColor)
                .size(contentBorderWidth)
                .offset(x = contentBorderWidth, y = contentBorderWidth)
                .align(Alignment.BottomEnd)
        )
    }
}

@Composable
internal fun rememberResizeState(
    initialWidth: Dp?,
    initialHeight: Dp?,
) = remember(
    initialWidth,
    initialHeight,
) {
    ResizeState(
        initialWidth = initialWidth,
        initialHeight = initialHeight,
    )
}

internal class ResizeState(
    private val initialWidth: Dp?,
    private val initialHeight: Dp?,
) {
    internal var controlledWidth by mutableStateOf(initialWidth)
    internal var controlledHeight by mutableStateOf(initialHeight)

    internal var contentWidth by mutableStateOf<Dp?>(null)
    internal var contentHeight by mutableStateOf<Dp?>(null)

    internal fun resetSize() {
        if (contentWidth != null) contentWidth = null
        if (contentHeight != null) contentHeight = null
        controlledWidth = initialWidth
        controlledHeight = initialHeight
    }

    internal fun setSize(width: Dp, height: Dp) {
        controlledWidth = width
        controlledHeight = height
    }
}

private fun Modifier.resizable(
    state: ResizeState,
) = composed {
    val density = LocalDensity.current
    var lastSize by remember { mutableStateOf<DpSize?>(null) }
    LaunchedEffect(state.contentWidth == null || state.contentHeight == null) {
        state.contentWidth = lastSize?.width
        state.contentHeight = lastSize?.height
    }
    onSizeChanged {
        with(density) {
            val widthDp = it.width.toDp()
            val heightDp = it.height.toDp()
            lastSize = DpSize(widthDp, heightDp)
            state.contentWidth = widthDp
            state.contentHeight = heightDp
        }
    }.thenIfNotNull(state.controlledWidth) {
        width(it)
    }.thenIfNotNull(state.controlledHeight) {
        height(it)
    }
}

private fun Modifier.resizableHandle(
    state: ResizeState,
    direction: ResizableHandleDirection,
) = then(
    Modifier.pointerInput(Unit) {
        detectDragGestures { change, _ ->
            if (direction.isContainX) {
                val changeX = change.positionChange().x.toDp()
                (state.controlledWidth ?: state.contentWidth)?.let {
                    state.controlledWidth = it + changeX
                }
            }

            if (direction.isContainY) {
                val changeY = change.positionChange().y.toDp()
                (state.controlledHeight ?: state.contentHeight)?.let {
                    state.controlledHeight = it + changeY
                }
            }

            change.consume()
        }
    }.thenIf(direction.isResettable) {
        pointerInput(Unit) {
            detectTapGestures(
                onDoubleTap = {
                    state.resetSize()
                },
            )
        }
    }.pointerHoverIcon(direction.cursor)
)

private enum class ResizableHandleDirection(
    val isContainX: Boolean,
    val isContainY: Boolean,
    val cursor: PointerIcon,
    val isResettable: Boolean = false,
) {
    X(isContainX = true, isContainY = false, cursor = PointerIcon.Hand),
    Y(isContainX = false, isContainY = true, PointerIcon.Hand),
    Both(isContainX = true, isContainY = true, PointerIcon.Crosshair, isResettable = true),
}
