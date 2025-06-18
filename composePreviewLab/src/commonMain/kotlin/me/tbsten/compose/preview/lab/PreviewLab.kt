package me.tbsten.compose.preview.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import me.tbsten.compose.preview.lab.component.Divider
import me.tbsten.compose.preview.lab.component.EventListSection
import me.tbsten.compose.preview.lab.component.FieldListSection
import me.tbsten.compose.preview.lab.component.LayoutSection
import me.tbsten.compose.preview.lab.component.PreviewLabHeader
import me.tbsten.compose.preview.lab.field.ScreenSize
import me.tbsten.compose.preview.lab.field.ScreenSizeField
import me.tbsten.compose.preview.lab.theme.AppTheme
import me.tbsten.compose.preview.lab.util.toDpOffset

open class PreviewLab {
    @Composable
    operator fun invoke(
        state: PreviewLabState = rememberSaveable(saver = PreviewLabState.Saver) { PreviewLabState() },
        maxWidth: Dp,
        maxHeight: Dp,
        content: @Composable PreviewLabScope.() -> Unit,
    ) = invoke(
        state = state,
        screenSizes = listOf(ScreenSize(maxWidth, maxHeight)),
        content = content,
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    operator fun invoke(
        state: PreviewLabState = rememberSaveable(saver = PreviewLabState.Saver) { PreviewLabState() },
        screenSizes: List<ScreenSize> = ScreenSize.SmartphoneAndDesktops,
        content: @Composable PreviewLabScope.() -> Unit,
    ) = AppTheme {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            PreviewLabHeader(
                scale = state.contentScale,
                onScaleChange = { state.contentScale = it },
            ) {
                CompositionLocalProvider(
                    LocalPreviewLabState provides state,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    ) {
                        ContentSection(
                            state = state,
                            screenSizes = screenSizes,
                            content = content,
                            modifier = Modifier
                                .weight(1f)
                                .zIndex(-1f),
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

    companion object : PreviewLab()
}

internal val LocalPreviewLabState = compositionLocalOf<PreviewLabState?> { null }

@Composable
private fun ContentSection(
    state: PreviewLabState,
    screenSizes: List<ScreenSize>,
    modifier: Modifier = Modifier,
    content: @Composable PreviewLabScope.() -> Unit,
) {
    val density = LocalDensity.current
    val screenSize = state.scope
        .fieldValue {
            ScreenSizeField(
                sizes = screenSizes,
            )
        }

    Box(
        modifier = modifier
            .zIndex(-1f)
            .draggable2D(state.contentDraggableState)
            .graphicsLayer {
                translationX = state.contentOffset.x
                translationY = state.contentOffset.y
                scaleX = state.contentScale
                scaleY = state.contentScale
                transformOrigin = TransformOrigin(0f, 0f)
            },
    ) {
        Box(
            modifier = Modifier
                .padding(32.dp)
                .layout { m, c ->
                    val p = m.measure(
                        c.copy(
                            maxWidth = screenSize?.width?.roundToPx() ?: c.maxWidth,
                            maxHeight = screenSize?.height?.roundToPx() ?: c.maxHeight,
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
                    .border(8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    .padding(8.dp)
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
private fun SideTabsSection(state: PreviewLabState) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .width(250.dp)
            .fillMaxHeight(),
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
                        contentRootOffset = state.contentRootOffsetInAppRoot,
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
