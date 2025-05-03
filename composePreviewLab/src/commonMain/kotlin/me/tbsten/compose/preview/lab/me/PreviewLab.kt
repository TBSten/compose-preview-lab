package me.tbsten.compose.preview.lab.me

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import me.tbsten.compose.preview.lab.me.component.CommonIconButton
import me.tbsten.compose.preview.lab.me.component.ConfigurationSelector
import me.tbsten.compose.preview.lab.me.component.FieldListSection
import me.tbsten.compose.preview.lab.me.component.ResizableBox
import me.tbsten.compose.preview.lab.me.component.rememberResizeState

@Composable
fun PreviewLab(
    name: String = PreviewLabConfiguration.Default.name,
    maxWidth: Dp? = PreviewLabConfiguration.Default.maxWidth,
    maxHeight: Dp? = PreviewLabConfiguration.Default.maxHeight,
    content: @Composable PreviewLabScope.() -> Unit,
) = PreviewLab(
    configurations = listOf(
        PreviewLabConfiguration(
            name = name,
            maxWidth = maxWidth,
            maxHeight = maxHeight
        )
    ),
    content = content,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewLab(
    configurations: List<PreviewLabConfiguration> = listOf(PreviewLabConfiguration.Default),
    content: @Composable PreviewLabScope.() -> Unit,
) {
    check(configurations.isNotEmpty())

    Column {
        ConfigurationSelector(
            configurations = configurations,
        ) { conf ->
            val scope = remember { PreviewLabScope() }

            CompositionLocalProvider(
                LocalPreviewLabScope provides scope,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    var scale by remember { mutableStateOf(1f) }
                    var offset by remember { mutableStateOf(Offset.Zero) }
                    val draggableState = rememberDraggable2DState { offset += it }
                    val resizeState = rememberResizeState(conf.maxWidth, conf.maxHeight)

                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            contentAlignment = Alignment.TopStart,
                            modifier = Modifier
                                .draggable2D(draggableState)
                                .graphicsLayer {
                                    translationX = offset.x
                                    translationY = offset.y
                                    transformOrigin = TransformOrigin(0f, 0f)
                                }
                                .zIndex(-1f)
                                .weight(1f)
                                .fillMaxSize()
                                .padding(32.dp)
                                .layout { m, c ->
                                    val p = m.measure(
                                        c.copy(
                                            maxWidth = Constraints.Infinity,
                                            maxHeight = Constraints.Infinity,
                                        )
                                    )
                                    layout(p.width, p.height) {
                                        val x =
                                            if (c.maxWidth <= p.width)
                                                -((c.maxWidth - p.width) / 2)
                                            else 0
                                        val y =
                                            if (c.maxHeight <= p.height)
                                                -((c.maxHeight - p.height) / 2)
                                            else 0
                                        p.place(x, y)
                                    }
                                }
                        ) {
                            ResizableBox(
                                maxWidth = conf.maxWidth,
                                maxHeight = conf.maxHeight,
                                resizeState = resizeState,
                                contentScale = scale,
                                modifier = Modifier
                            ) {
                                content(scope)
                            }
                        }

                        HorizontalDivider()

                        // TODO move to header
                        Row(
                            Modifier
                                .background(MaterialTheme.colorScheme.background)
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            CommonIconButton(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = "Zoom In",
                                enabled = scale < MaxZoomScale,
                                onClick = {
                                    scale = scale.nextZoomInScale()
                                },
                            )

                            CommonIconButton(
                                imageVector = Icons.Default.ZoomOut,
                                contentDescription = "Zoom Out",
                                enabled = MinZoomScale < scale,
                                onClick = {
                                    scale = scale.nextZoomOutScale()
                                },
                            )

                            CommonIconButton(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Zoom Reset",
                                onClick = {
                                    scale = 1.00f
                                },
                            )
                        }
                    }

                    VerticalDivider()
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .width(300.dp)
                            .fillMaxHeight()
                    ) {
                        // TODO events, layouts
                        FieldListSection(
                            fields = scope.fields,
                        )
                    }
                }
            }
        }
    }
}

private const val MinZoomScale = 0.10f
private const val MaxZoomScale = 10.00f
private fun Float.nextZoomInScale(): Float = when (this) {
    in Float.MIN_VALUE..<1.0f -> this + 0.10f
    in 1.0f..<2.0f -> this + 0.25f
    in 1.0f..<Float.MAX_VALUE -> this + 1.00f
    else -> TODO("Zoom value is out of range: $this")
}

private fun Float.nextZoomOutScale(): Float = when (this) {
    in Float.MIN_VALUE..<1.0f -> this - 0.10f
    in 1.0f..<2.0f -> this - 0.25f
    in 1.0f..<Float.MAX_VALUE -> this - 1.00f
    else -> TODO("Zoom value is out of range: $this")
}

internal val LocalPreviewLabScope = compositionLocalOf<PreviewLabScope?> { null }
