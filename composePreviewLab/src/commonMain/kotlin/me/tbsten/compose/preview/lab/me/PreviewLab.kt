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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import me.tbsten.compose.preview.lab.me.component.Divider
import me.tbsten.compose.preview.lab.me.component.EventListSection
import me.tbsten.compose.preview.lab.me.component.FieldListSection
import me.tbsten.compose.preview.lab.me.component.PreviewLabHeader
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

    var offset by remember { mutableStateOf(Offset.Zero) }
    val draggableState = rememberDraggable2DState { offset += it }
    var scale by remember { mutableStateOf(1f) }

    Column {
        PreviewLabHeader(
            configurations = configurations,
            scale = scale,
            onScaleChange = { scale = it },
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
                            ) {
                                content(scope)
                            }
                        }
                    }

                    Divider()

                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .width(300.dp)
                            .fillMaxHeight()
                    ) {
                        var selectedTab by remember { mutableStateOf(0) }
                        val tabContents = remember {
                            mapOf<String, @Composable () -> Unit>(
                                "Fields" to {
                                    FieldListSection(
                                        fields = scope.fields,
                                    )
                                },
                                "Events" to {
                                    EventListSection(
                                        events = scope.events,
                                        onClear = { scope.events.clear() },
                                    )
                                },
                                "Layouts" to {
                                },
                            )
                        }
                        val pagerState = rememberPagerState { tabContents.size }
                            .also {
                                LaunchedEffect(selectedTab) {
                                    it.animateScrollToPage(
                                        selectedTab
                                    )
                                }
                            }

                        ScrollableTabRow(
                            selectedTabIndex = selectedTab,
                            edgePadding = 0.dp,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            tabContents.keys.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = { Text(title) },
                                )
                            }
                        }

                        HorizontalPager(
                            state = pagerState,
                            userScrollEnabled = false,
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.Top,
                        ) { pageIndex ->
                            val (_, content) = tabContents.entries.toList()[pageIndex]
                            content()
                        }
                    }

                }
            }
        }
    }
}

internal val LocalPreviewLabScope = compositionLocalOf<PreviewLabScope?> { null }
