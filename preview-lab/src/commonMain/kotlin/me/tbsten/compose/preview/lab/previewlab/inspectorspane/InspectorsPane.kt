package me.tbsten.compose.preview.lab.previewlab.inspectorspane

import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import me.tbsten.compose.preview.lab.LocalPreviewLabPreview
import me.tbsten.compose.preview.lab.previewlab.PreviewLabState
import me.tbsten.compose.preview.lab.previewlab.openfilehandler.LocalOpenFileHandler
import me.tbsten.compose.preview.lab.previewlab.openfilehandler.OpenFileHandler
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.adaptive
import me.tbsten.compose.preview.lab.ui.components.PreviewLabButton
import me.tbsten.compose.preview.lab.ui.components.PreviewLabButtonVariant
import me.tbsten.compose.preview.lab.ui.components.PreviewLabDivider
import me.tbsten.compose.preview.lab.ui.components.PreviewLabIcon
import me.tbsten.compose.preview.lab.ui.components.PreviewLabIconButton
import me.tbsten.compose.preview.lab.ui.components.PreviewLabIconButtonVariant
import me.tbsten.compose.preview.lab.ui.components.PreviewLabModal
import me.tbsten.compose.preview.lab.ui.components.PreviewLabTabPager
import me.tbsten.compose.preview.lab.ui.components.PreviewLabText
import me.tbsten.compose.preview.lab.ui.generated.resources.PreviewLabUiRes
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_code
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun InspectorsPane(
    state: PreviewLabState,
    isVisible: Boolean,
    inspectorTabs: List<InspectorTab> = InspectorTab.defaults,
    content: @Composable () -> Unit,
) {
    val content = remember(content) { movableContentOf { content() } }

    if (!isVisible) {
        content()
        return
    }

    val allTabs = inspectorTabs

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
                    allTabs.forEachIndexed { index, tab ->
                        tab.icon?.let { icon ->
                            PreviewLabIconButton(
                                variant = PreviewLabIconButtonVariant.PrimaryElevated,
                                painter = icon(),
                                contentDescription = tab.title,
                                onClick = { state.selectedTabIndex = index },
                            )
                        }

                        PreviewLabModal(
                            isVisible = state.selectedTabIndex == index,
                            onDismissRequest = { state.deselectTab() },
                            contentPadding = PaddingValues(20.dp),
                        ) {
                            Box(
                                Modifier
                                    .background(PreviewLabTheme.colors.background, shape = RoundedCornerShape(8.dp))
                                    .heightIn(min = 200.dp),
                            ) {
                                with(tab) {
                                    InspectorTab.ContentContext(
                                        state = state,
                                        inspectorTabs = allTabs,
                                    ).Content()
                                }
                            }
                        }
                    }

                    val openHandler = LocalOpenFileHandler.current
                    val filePath = LocalPreviewLabPreview.current?.filePath
                    val startLineNumber = LocalPreviewLabPreview.current?.startLineNumber

                    if (filePath != null && openHandler != null) {
                        val configuredValue = openHandler.configure()
                        PreviewLabIconButton(
                            variant = PreviewLabIconButtonVariant.PrimaryOutlined,
                            painter = painterResource(PreviewLabUiRes.drawable.icon_code),
                            contentDescription = "Show source code",
                            onClick = {
                                @Suppress("UNCHECKED_CAST")
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

            Row {
                content()
                PreviewLabDivider()
                Column(
                    modifier = Modifier
                        .background(PreviewLabTheme.colors.background)
                        .width(250.dp)
                        .fillMaxHeight(),
                ) {
                    PreviewLabTabPager(
                        tabs = allTabs,
                        title = { it.title },
                        pagerState = rememberPagerState { allTabs.size }
                            .also { pagerState ->
                                LaunchedEffect(state.selectedTabIndex) {
                                    state.selectedTabIndex?.let {
                                        pagerState.animateScrollToPage(it)
                                    }
                                }
                                LaunchedEffect(Unit) {
                                    snapshotFlow { pagerState.targetPage }
                                        .collectLatest {
                                            state.selectedTabIndex = it
                                        }
                                }
                            },
                        modifier = Modifier.weight(1f),
                    ) { tab ->
                        with(tab) {
                            InspectorTab.ContentContext(
                                state = state,
                                inspectorTabs = allTabs,
                            ).Content()
                        }
                    }

                    val startLineNumber = LocalPreviewLabPreview.current?.startLineNumber
                    val filePath = LocalPreviewLabPreview.current?.filePath
                    val openHandler = LocalOpenFileHandler.current
                    if (filePath != null && openHandler != null) {
                        PreviewLabDivider()

                        val configuredValue = openHandler.configure()
                        PreviewLabButton(
                            variant = PreviewLabButtonVariant.PrimaryOutlined,
                            onClick = {
                                @Suppress("UNCHECKED_CAST")
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
                            PreviewLabIcon(
                                painter = painterResource(PreviewLabUiRes.drawable.icon_code),
                                contentDescription = null,
                            )
                            Spacer(Modifier.width(8.dp))
                            PreviewLabText(
                                text = "Source Code",
                            )
                        }
                    }
                }
            }
        },
    )
}
