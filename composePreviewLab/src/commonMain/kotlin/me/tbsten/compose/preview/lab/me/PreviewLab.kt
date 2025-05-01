package me.tbsten.compose.preview.lab.me

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import me.tbsten.compose.preview.lab.me.component.ConfigurationSelector
import me.tbsten.compose.preview.lab.me.component.FieldListSection
import me.tbsten.compose.preview.lab.me.util.thenIfNotNull

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

                    Box(
                        contentAlignment = Alignment.TopCenter,
                        modifier = Modifier
                            .zIndex(-1f)
                            .weight(1f)
                            .fillMaxHeight()
                            .draggable2D(rememberDraggable2DState { offset += it })
                            .transformable(
                                rememberTransformableState { zoomChange, offsetChange, _ ->
                                    scale *= zoomChange
                                    offset += offsetChange
                                }
                            )
                            .graphicsLayer {
                                translationX = offset.x
                                translationY = offset.y
                                scaleX = scale
                                scaleY = scale
                            }.padding(32.dp)
                            .layout { m, c ->
                                val p = m.measure(
                                    c.copy(
                                        maxWidth = Constraints.Infinity,
                                        maxHeight = Constraints.Infinity,
                                    )
                                )
                                layout(p.width, p.height) {
                                    val x =
                                        if (c.maxWidth <= p.width) -((c.maxWidth - p.width) / 2) else 0
                                    val y =
                                        if (c.maxHeight <= p.height) -((c.maxHeight - p.height) / 2) else 0
                                    p.place(x, y)
                                }
                            }

                    ) {
                        Box(
                            modifier = Modifier
                                .border(
                                    width = 8.dp,
                                    brush = Brush.linearGradient(
                                        0f to MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                        1f to MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                    ),
                                    shape = RectangleShape,
                                )
                                .padding(8.dp)
                                .thenIfNotNull(conf.maxHeight) { heightIn(max = it) }
                                .thenIfNotNull(conf.maxWidth) { widthIn(max = it) }
                        ) {
                            content(scope)
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

internal val LocalPreviewLabScope = compositionLocalOf<PreviewLabScope?> { null }
