package me.tbsten.compose.preview.lab.me

import androidx.compose.foundation.background
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import me.tbsten.compose.preview.lab.me.component.Divider
import me.tbsten.compose.preview.lab.me.component.EventListSection
import me.tbsten.compose.preview.lab.me.component.FieldListSection
import me.tbsten.compose.preview.lab.me.component.LayoutSection
import me.tbsten.compose.preview.lab.me.component.PreviewLabHeader
import me.tbsten.compose.preview.lab.me.component.ResizableBox
import me.tbsten.compose.preview.lab.me.component.rememberResizeState
import me.tbsten.compose.preview.lab.me.theme.AppTheme

@Composable
fun PreviewLab(
    name: String = PreviewLabConfiguration.Default.name,
    maxWidth: Dp? = PreviewLabConfiguration.Default.maxWidth,
    maxHeight: Dp? = PreviewLabConfiguration.Default.maxHeight,
    state: PreviewLabState = remember { PreviewLabState() },
    content: @Composable PreviewLabScope.() -> Unit,
) = PreviewLab(
    configurations = listOf(
        PreviewLabConfiguration(
            name = name,
            maxWidth = maxWidth,
            maxHeight = maxHeight
        )
    ),
    state = state,
    content = content,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewLab(
    configurations: List<PreviewLabConfiguration> = listOf(PreviewLabConfiguration.Default),
    state: PreviewLabState = remember { PreviewLabState() },
    content: @Composable PreviewLabScope.() -> Unit,
) = AppTheme {
    check(configurations.isNotEmpty())

    Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        PreviewLabHeader(
            configurations = configurations,
            scale = state.contentScale,
            onScaleChange = { state.contentScale = it },
        ) { conf ->
            CompositionLocalProvider(
                LocalPreviewLabState provides state,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    ContentSection(
                        conf = conf,
                        state = state,
                        content = content,
                        modifier = Modifier
                            .weight(1f)
                            .zIndex(-1f)
                    )

                    Divider()

                    SideTabsSection(
                        state = state,
                    )
                }
            }
        }
    }
}

internal val LocalPreviewLabState = compositionLocalOf<PreviewLabState?> { null }

@Composable
private fun ContentSection(
    conf: PreviewLabConfiguration,
    state: PreviewLabState,
    modifier: Modifier = Modifier,
    content: @Composable PreviewLabScope.() -> Unit,
) {
    val resizeState = rememberResizeState(conf.maxWidth, conf.maxHeight)

    Column(modifier = modifier) {
        Box(
            contentAlignment = Alignment.TopStart,
            modifier = Modifier
                .previewLabContent(state)
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
                contentScale = state.contentScale,
            ) {
                content(state.scope)
            }
        }
    }
}

@Composable
private fun SideTabsSection(
    state: PreviewLabState,
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .width(250.dp)
            .fillMaxHeight()
    ) {
        val tabContents = remember {
            mapOf<String, @Composable () -> Unit>(
                "Fields" to {
                    FieldListSection(
                        fields = state.scope.fields,
                    )
                },
                "Events" to {
                    EventListSection(
                        events = state.scope.events,
                        onClear = { state.scope.events.clear() },
                    )
                },
                "Layouts" to {
                    LayoutSection(
                        layoutNodes = state.scope.layoutNodes,
                        selectedLayoutNodeIds = state.scope.selectedLayoutNodeIds,
                        hoveredLayoutNodeIds = state.scope.hoveredLayoutNodeIds,
                        onNodeClick = state.scope::toggleLayoutNodeSelect,
                    )
                },
            )
        }
        val pagerState = rememberPagerState { tabContents.size }
            .also {
                LaunchedEffect(state.selectedTabIndex) {
                    it.animateScrollToPage(
                        state.selectedTabIndex,
                    )
                }
            }

        ScrollableTabRow(
            selectedTabIndex = state.selectedTabIndex,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            tabContents.keys.forEachIndexed { index, title ->
                Tab(
                    selected = state.selectedTabIndex == index,
                    onClick = { state.selectedTabIndex = index },
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
