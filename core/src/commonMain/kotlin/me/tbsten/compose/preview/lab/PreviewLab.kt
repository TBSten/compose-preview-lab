package me.tbsten.compose.preview.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
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
import com.dokar.sonner.TextToastAction
import com.dokar.sonner.Toaster
import com.dokar.sonner.ToasterState
import com.dokar.sonner.rememberToasterState
import me.tbsten.compose.preview.lab.component.CommonIconButton
import me.tbsten.compose.preview.lab.component.Divider
import me.tbsten.compose.preview.lab.component.EventListSection
import me.tbsten.compose.preview.lab.component.FieldListSection
import me.tbsten.compose.preview.lab.component.PreviewLabHeader
import me.tbsten.compose.preview.lab.component.SimpleModal
import me.tbsten.compose.preview.lab.component.TabPager
import me.tbsten.compose.preview.lab.component.adaptive
import me.tbsten.compose.preview.lab.core.generated.resources.Res
import me.tbsten.compose.preview.lab.core.generated.resources.icon_code
import me.tbsten.compose.preview.lab.core.generated.resources.icon_edit
import me.tbsten.compose.preview.lab.core.generated.resources.icon_history
import me.tbsten.compose.preview.lab.field.ScreenSize
import me.tbsten.compose.preview.lab.field.ScreenSizeField
import me.tbsten.compose.preview.lab.openfilehandler.LocalOpenFileHandler
import me.tbsten.compose.preview.lab.openfilehandler.OpenFileHandler
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Button
import me.tbsten.compose.preview.lab.ui.components.ButtonVariant
import me.tbsten.compose.preview.lab.ui.components.Icon
import me.tbsten.compose.preview.lab.ui.components.IconButtonVariant
import me.tbsten.compose.preview.lab.ui.components.Text
import me.tbsten.compose.preview.lab.util.toDpOffset
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * PreviewLab is a composable function that provides a preview environment for Compose UI components.
 * Since the companion object is PreviewLab, you can use this to define Preview as follows to use powerful functions such as Field, Event, etc.
 *
 * ```kt
 * @Composable
 * private fun MyButtonPreview() = PreviewLab {
 *   MyButton()
 * }
 * ```
 *
 * In the future, PreviewLab is defined as a class in order to share common PreviewLab settings.
 *
 * ```kt
 * val myPreviewLab = PreviewLab()
 *
 * @Composable
 * private fun MyButtonPreview() = myPreviewLab {
 *   MyButton()
 * }
 * ```
 *
 * However, since there are currently no settings that can be shared, it may not be necessary to define your own PreviewLab.
 *
 * @param defaultState Default PreviewLabState factory.
 * @param defaultScreenSizes Default list of ScreenSize.
 * @param contentRoot Wrapper for the entire PreviewLab.
 * @param disableTrailingLambda This argument has no meaning and exists to avoid defining the `PreviewLab.invoke` method, which is the main part of PreviewLab, as a trailing lambda.
 * @see PreviewLab.invoke
 * @see PreviewLabState
 * @see PreviewLabScope
 * @see ScreenSize
 */
open class PreviewLab(
    private val defaultState: @Composable () -> PreviewLabState =
        { rememberSaveable(saver = PreviewLabState.Saver) { PreviewLabState() } },
    private val defaultScreenSizes: List<ScreenSize> = ScreenSize.SmartphoneAndDesktops,
    private val contentRoot: @Composable (content: @Composable () -> Unit) -> Unit = { it() },
    @Suppress("unused") disableTrailingLambda: Nothing? = null,
) {
    /**
     * Short hand for PreviewLab.invoke() specifying a single screensize.
     *
     * @see PreviewLab.invoke
     */
    @Composable
    operator fun invoke(
        state: PreviewLabState = defaultState(),
        maxWidth: Dp,
        maxHeight: Dp,
        content: @Composable PreviewLabScope.() -> Unit,
    ) = invoke(
        state = state,
        screenSizes = listOf(ScreenSize(maxWidth, maxHeight)),
        content = content,
    )

    /**
     * Enclosing the top level of Preview with calls to this function provides features such as Field, Event, and Layout.
     * Data generated by functions such as `fieldValue` and `onEvent` are stored in PreviewLabScope and PreviewLabState and displayed in the right sidebar.
     *
     * ```kt
     * @Preview
     * @Composable
     * private fun MyButtonPreview() = PreviewLab { // this: PreviewLabScope
     *   MyButton(
     *     text = fieldValue { StringField(label = "MyButton.text", defaultValue = "Click Me!") },
     *     onClick = { onEvent("MyButton.onClick") },
     *   )
     * }
     * ```
     *
     * @param state [PreviewLabState] Specify the PreviewLabState to be used in displaying this Preview.
     * @param screenSizes List of [ScreenSize] to be used in displaying this Preview. If there are Composable objects that fill the screen, such as fillMaxSize(), their size is limited by the value specified in this screenSizes argument; setting emptyList() will result in an error.
     * @param content Preview Contents. From the receiver's PreviewLabScope, you can use features such as `fieldValue` and `onEvent` to make Preview more powerful.
     *
     * @see PreviewLabState
     * @see ScreenSize
     */
    @Composable
    operator fun invoke(
        state: PreviewLabState = defaultState(),
        screenSizes: List<ScreenSize> = defaultScreenSizes,
        content: @Composable PreviewLabScope.() -> Unit,
    ) {
        val toaster = rememberToasterState().also { toaster ->
            state.scope.HandleEvents { event ->
                when (event) {
                    is PreviewLabScope.Event.ShowEventToast ->
                        toaster.show(
                            message = event.event.title,
                            action = TextToastAction(
                                text = "Show Detail",
                                onClick = {
                                    state.selectedTabIndex = InspectorTab.entries.indexOf(InspectorTab.Events)
                                    state.selectedEvent = event.event
                                    toaster.dismiss(it)
                                },
                            ),
                        )
                }
            }
        }

        Providers(state = state, toaster = toaster) {
            Column(modifier = Modifier.background(PreviewLabTheme.colors.background)) {
                PreviewLabHeader(
                    scale = state.contentScale,
                    onScaleChange = { state.contentScale = it },
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

            Toaster(
                state = toaster,
                maxVisibleToasts = 10,
                showCloseButton = true,
            )
        }
    }

    @Composable
    private fun Providers(state: PreviewLabState, toaster: ToasterState, content: @Composable () -> Unit) {
        contentRoot {
            PreviewLabTheme {
                CompositionLocalProvider(
                    LocalPreviewLabState provides state,
                    LocalToaster provides toaster,
                ) {
                    content()
                }
            }
        }
    }

    /**
     * PreviewLab with default settings.
     */
    companion object : PreviewLab()
}

internal val LocalPreviewLabState = compositionLocalOf<PreviewLabState?> { null }
internal val LocalToaster = compositionLocalOf<ToasterState> { error("No ToasterState") }

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
                    .border(8.dp, PreviewLabTheme.colors.outline.copy(alpha = 0.25f))
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
private fun InspectorsPane(state: PreviewLabState, content: @Composable () -> Unit) {
    val tabContent = remember {
        movableContentOf { tab: InspectorTab ->
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
                        CommonIconButton(
                            variant = IconButtonVariant.PrimaryElevated,
                            painter = painterResource(tab.iconRes),
                            contentDescription = tab.title,
                            onClick = { state.selectedTabIndex = index },
                        )

                        SimpleModal(
                            isVisible = state.selectedTabIndex == index,
                            onDismissRequest = { state.deselectTab() },
                            contentPadding = PaddingValues(20.dp),
                        ) {
                            Box(
                                Modifier
                                    .background(PreviewLabTheme.colors.background, shape = RoundedCornerShape(8.dp))
                                    .heightIn(min = 200.dp),
                            ) {
                                tabContent(tab)
                            }
                        }
                    }

                    val openHandler = LocalOpenFileHandler.current
                    val filePath = LocalCollectedPreview.current?.filePath
                    val startLineNumber = LocalCollectedPreview.current?.startLineNumber

                    if (filePath != null && openHandler != null) {
                        val configuredValue = openHandler.configure()
                        CommonIconButton(
                            variant = IconButtonVariant.PrimaryOutlined,
                            painter = painterResource(Res.drawable.icon_code),
                            contentDescription = "Show source code",
                            onClick = {
                                (openHandler as OpenFileHandler<in Any?>).openFile(
                                    OpenFileHandler.Params(
                                        configuredValue = configuredValue,
                                        filePathInProject = filePath,
                                        startLineNumber = startLineNumber,
                                    ),
                                )
                            },
                        )
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
                        .background(PreviewLabTheme.colors.background)
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
                                LaunchedEffect(Unit) {
                                    snapshotFlow { pagerState.currentPage }
                                        .collect {
                                            state.selectedTabIndex = it
                                        }
                                }
                            },
                        modifier = Modifier.weight(1f),
                    ) {
                        tabContent(it)
                    }

                    val startLineNumber = LocalCollectedPreview.current?.startLineNumber
                    val filePath = LocalCollectedPreview.current?.filePath
                    val openHandler = LocalOpenFileHandler.current
                    if (filePath != null && openHandler != null) {
                        Divider()

                        val configuredValue = openHandler.configure()
                        Button(
                            variant = ButtonVariant.PrimaryOutlined,
                            onClick = {
                                (openHandler as OpenFileHandler<in Any?>).openFile(
                                    OpenFileHandler.Params(
                                        configuredValue = configuredValue,
                                        filePathInProject = filePath,
                                        startLineNumber = startLineNumber,
                                    ),
                                )
                            },
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
                selectedEvent = state.selectedEvent,
                onClear = { state.scope.events.clear() },
            )
        },
    ),
    // TODO Add Layouts Inspector tab
//    Layouts(
//        title = "Layouts",
//        iconRes = Res.drawable.icon_dashboard,
//        content = { state ->
//            LayoutSection(
//                contentRootOffset = state.contentRootOffsetInAppRoot,
//                layoutNodes = state.scope.layoutNodes,
//                selectedLayoutNodeIds = state.scope.selectedLayoutNodeIds,
//                hoveredLayoutNodeIds = state.scope.hoveredLayoutNodeIds,
//                onNodeClick = state.scope::toggleLayoutNodeSelect,
//            )
//        },
//    ),
}
