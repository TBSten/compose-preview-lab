package me.tbsten.compose.preview.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.movableContentOf
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
import me.tbsten.compose.preview.lab.component.SimpleBottomSheet
import me.tbsten.compose.preview.lab.component.TabPager
import me.tbsten.compose.preview.lab.component.adaptive
import me.tbsten.compose.preview.lab.composepreviewlab.generated.resources.Res
import me.tbsten.compose.preview.lab.composepreviewlab.generated.resources.icon_code
import me.tbsten.compose.preview.lab.composepreviewlab.generated.resources.icon_dashboard
import me.tbsten.compose.preview.lab.composepreviewlab.generated.resources.icon_edit
import me.tbsten.compose.preview.lab.composepreviewlab.generated.resources.icon_history
import me.tbsten.compose.preview.lab.field.ScreenSize
import me.tbsten.compose.preview.lab.field.ScreenSizeField
import me.tbsten.compose.preview.lab.openfilehandler.LocalOpenFileHandler
import me.tbsten.compose.preview.lab.openfilehandler.OpenFileHandler
import me.tbsten.compose.preview.lab.theme.AppTheme
import me.tbsten.compose.preview.lab.util.toDpOffset
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

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
                        InspectorsPane(state = state) {
                            ContentSection(
                                state = state,
                                screenSizes = screenSizes,
                                content = content,
                                modifier = Modifier
                                    .weight(1f)
                                    .zIndex(-1f),
                            )
                        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InspectorsPane(state: PreviewLabState, content: @Composable () -> Unit) {
    val tabContent = remember {
        movableContentOf { tab: InspectorTab, state: PreviewLabState ->
            tab.content(state)
        }
    }
    // エラーになるので movableContentOf を使わない
//    val content = remember { movableContentOf(content) }

    adaptive(
        small = {
            // Show Inspector as a button on small screens

            BoxWithConstraints(Modifier.fillMaxSize()) {
                content()

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .sizeIn(maxWidth = maxWidth / 3, maxHeight = maxHeight * 2 / 3),
                ) {
                    InspectorTab.entries.forEachIndexed { index, tab ->
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                            tooltip = { PlainTooltip { Text(tab.title) } },
                            state = rememberTooltipState(),
                        ) {
                            SmallFloatingActionButton(
                                onClick = { state.selectedTabIndex = index },
                            ) {
                                Icon(
                                    painter = painterResource(tab.iconRes),
                                    contentDescription = tab.title,
                                )
                            }
                        }

                        if (state.selectedTabIndex == index) {
                            SimpleBottomSheet(onDismissRequest = { state.deselectTab() }) {
                                Box(Modifier.heightIn(min = 200.dp)) {
                                    tab.content(state)
                                }
                            }
                        }
                    }

                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = { PlainTooltip { Text("Show Source Code") } },
                        state = rememberTooltipState(),
                    ) {
                        val filePath = LocalCollectedPreview.current?.filePath
                        val openHandler = LocalOpenFileHandler.current
                        if (filePath != null && openHandler != null) {
                            val configuredValue = openHandler.configure()
                            SmallFloatingActionButton(
                                onClick = { (openHandler as OpenFileHandler<in Any?>).openFile(configuredValue, filePath) },
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.icon_code),
                                    contentDescription = "Show source code",
                                )
                            }
                        }
                    }
                }
            }
        },
        medium = {
            // Show Inspector in tabs on large screens

            val tabContents = InspectorTab.entries

            Row {
                content()
                Divider()
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .width(250.dp)
                        .fillMaxHeight(),
                ) {
                    TabPager(
                        tabs = tabContents,
                        title = { it.title },
                        pagerState = rememberPagerState { tabContents.size }
                            .also { pagerState ->
                                LaunchedEffect(state.selectedTabIndex) {
                                    state.selectedTabIndex?.let {
                                        pagerState.animateScrollToPage(it)
                                    }
                                }
                            },
                        modifier = Modifier.weight(1f),
                    ) {
                        it.content(state)
                    }

                    val filePath = LocalCollectedPreview.current?.filePath
                    val openHandler = LocalOpenFileHandler.current
                    if (filePath != null && openHandler != null) {
                        Divider()

                        val configuredValue = openHandler.configure()
                        OutlinedButton(
                            onClick = { (openHandler as OpenFileHandler<in Any?>).openFile(configuredValue, filePath) },
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp).fillMaxWidth(),
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.icon_code),
                                contentDescription = null,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Source Code",
                            )
                        }
                    }
                }
            }
        },
    )
}

private enum class InspectorTab(
    val title: String,
    val iconRes: DrawableResource,
    val content: @Composable (state: PreviewLabState) -> Unit,
) {
    Fields(
        title = "Fields",
        iconRes = Res.drawable.icon_edit,
        content = { state ->
            FieldListSection(
                fields = state.scope.fields,
            )
        },
    ),
    Events(
        title = "Events",
        iconRes = Res.drawable.icon_history,
        content = { state ->
            EventListSection(
                events = state.scope.events,
                onClear = { state.scope.events.clear() },
            )
        },
    ),
    Layouts(
        title = "Layouts",
        iconRes = Res.drawable.icon_dashboard,
        content = { state ->
            LayoutSection(
                contentRootOffset = state.contentRootOffsetInAppRoot,
                layoutNodes = state.scope.layoutNodes,
                selectedLayoutNodeIds = state.scope.selectedLayoutNodeIds,
                hoveredLayoutNodeIds = state.scope.hoveredLayoutNodeIds,
                onNodeClick = state.scope::toggleLayoutNodeSelect,
            )
        },
    ),
}
