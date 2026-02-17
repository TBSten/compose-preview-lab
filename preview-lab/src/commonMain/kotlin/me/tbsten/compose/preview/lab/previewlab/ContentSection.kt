package me.tbsten.compose.preview.lab.previewlab

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.floor
import me.tbsten.compose.preview.lab.field.ScreenSize
import me.tbsten.compose.preview.lab.field.ScreenSizeField
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.toast.ToastHostState
import me.tbsten.compose.preview.lab.ui.util.thenIfNotNull
import me.tbsten.compose.preview.lab.ui.util.toDpOffset

@VisibleForTesting
val LocalEnforcePreviewLabState = compositionLocalOf<PreviewLabState?> { null }
internal val LocalToastHostState = compositionLocalOf<ToastHostState> { error("No ToastHostState") }

@Composable
internal fun ContentSection(
    state: PreviewLabState,
    screenSizes: List<ScreenSize>,
    showScreenSizeField: Boolean,
    graphicsLayer: GraphicsLayer,
    modifier: Modifier = Modifier,
    content: @Composable PreviewLabScope.() -> Unit,
) {
    val density = LocalDensity.current
    val screenSize = if (showScreenSizeField) {
        state.scope
            .fieldValue {
                ScreenSizeField(
                    sizes = screenSizes,
                )
            }
    } else {
        screenSizes.firstOrNull() ?: ScreenSize.MediumSmartPhone
    }

    Box(
        modifier = modifier
            .zIndex(-1f)
            .contentSectionBackground(state.contentOffset, state.contentScale, state.gridSize)
            .draggable2D(state.contentDraggableState)
            .graphicsLayer {
                translationX = state.contentOffset.x
                translationY = state.contentOffset.y
                scaleX = state.contentScale
                scaleY = state.contentScale
                transformOrigin = TransformOrigin(0f, 0f)
            }
            .fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .padding(32.dp)
                .layout { m, c ->
                    val p = m.measure(
                        c.copy(
                            maxWidth = screenSize.width.roundToPx(),
                            maxHeight = screenSize.height.roundToPx(),
                        ),
                    )
                    layout(p.width, p.height) {
                        val x =
                            if (c.maxWidth <= p.width) {
                                -((c.maxWidth - p.width) / 2)
                            } else {
                                0
                            }
                        val y =
                            if (c.maxHeight <= p.height) {
                                -((c.maxHeight - p.height) / 2)
                            } else {
                                0
                            }
                        p.place(x, y)
                    }
                },
        ) {
            Box(
                modifier = Modifier
                    .border(8.dp, PreviewLabTheme.colors.outline.copy(alpha = 0.25f))
                    .padding(8.dp)
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(graphicsLayer)
                    }
                    .onPlaced {
                        state.contentRootOffsetInAppRoot =
                            it.positionInRoot().toDpOffset(density)
                    },
            ) {
                content(state.scope)
            }
        }
    }
}

@Composable
private fun Modifier.contentSectionBackground(
    contentOffset: Offset,
    contentScale: Float,
    gridSize: Dp?,
    color: Color = PreviewLabTheme.colors.outlineSecondary,
) = thenIfNotNull(gridSize) { gridSize ->
    if (gridSize.value <= 0) {
        this
    } else {
        drawBehind {
            val gridSize = gridSize.toPx() * contentScale

            // grid
            val gridXStart = contentOffset.x - floor(contentOffset.x / gridSize) * gridSize
            var gridX = gridXStart
            while (gridX <= size.width) {
                drawLine(
                    color = color,
                    start = Offset(gridX, 0f),
                    end = Offset(gridX, size.height),
                    strokeWidth = 1.dp.toPx(),
                )

                gridX += gridSize
            }

            val gridYStart = contentOffset.y - floor(contentOffset.y / gridSize) * gridSize
            var gridY = gridYStart
            while (gridY <= size.height) {
                drawLine(
                    color = color,
                    start = Offset(0f, gridY),
                    end = Offset(size.width, gridY),
                    strokeWidth = 1.dp.toPx(),
                )

                gridY += gridSize
            }
        }
    }
}
