package me.tbsten.compose.preview.lab.component.inspectorspane

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
import me.tbsten.compose.preview.lab.LocalCollectedPreview
import me.tbsten.compose.preview.lab.PreviewLabState
import me.tbsten.compose.preview.lab.component.CommonIconButton
import me.tbsten.compose.preview.lab.component.Divider
import me.tbsten.compose.preview.lab.component.SimpleModal
import me.tbsten.compose.preview.lab.component.TabPager
import me.tbsten.compose.preview.lab.component.adaptive
import me.tbsten.compose.preview.lab.core.generated.resources.Res
import me.tbsten.compose.preview.lab.core.generated.resources.icon_code
import me.tbsten.compose.preview.lab.openfilehandler.LocalOpenFileHandler
import me.tbsten.compose.preview.lab.openfilehandler.OpenFileHandler
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Button
import me.tbsten.compose.preview.lab.ui.components.ButtonVariant
import me.tbsten.compose.preview.lab.ui.components.Icon
import me.tbsten.compose.preview.lab.ui.components.IconButtonVariant
import me.tbsten.compose.preview.lab.ui.components.Text
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun InspectorsPane(state: PreviewLabState, isVisible: Boolean, content: @Composable () -> Unit) {
//    val content = remember { movableContentOf { content() } }

    if (!isVisible) {
        content()
        return
    }

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
