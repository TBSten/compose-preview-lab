package me.tbsten.compose.preview.lab

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import me.tbsten.compose.preview.lab.component.Divider
import me.tbsten.compose.preview.lab.component.NoPreview
import me.tbsten.compose.preview.lab.component.NoSelectedPreview
import me.tbsten.compose.preview.lab.component.SimpleBottomSheet
import me.tbsten.compose.preview.lab.component.adaptive
import me.tbsten.compose.preview.lab.openfilehandler.LocalOpenFileHandler
import me.tbsten.compose.preview.lab.openfilehandler.OpenFileHandler
import me.tbsten.compose.preview.lab.previewlist.PreviewListTree
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme

/**
 * A Composable function that catalogs and displays a list of Previews. The left sidebar actually displays the list of Previews, and the selected Preview is displayed in the center of the screen.
 *
 * @param previews CollectedPreviews collected from gradle plugins, etc. Note that CollectedPreviews not specified here will not be displayed.
 * @param state [PreviewLabRootState] to manage the state of the PreviewLabRoot. Preserves the state of the selected Preview, etc. By default, remember is used (i.e., the composition of the call to Composable is the scope of the state), but the scope (storage period) of the state can be adjusted by moving it to a state holder, such as ViewModel, if necessary.
 * @param openFileHandler By specifying OpenFileHandler, you can display a "Source Code" button that displays the source code corresponding to the Preview.
 *
 * @see CollectedPreview
 * @see OpenFileHandler
 */
@Composable
fun PreviewLabRoot(
    previews: List<CollectedPreview>,
    modifier: Modifier = Modifier,
    state: PreviewLabRootState = remember { PreviewLabRootState() },
    openFileHandler: OpenFileHandler<out Any?>? = null,
) = PreviewLabTheme {
    CompositionLocalProvider(
        LocalOpenFileHandler provides openFileHandler,
    ) {
        val previewList = remember { previews.toList() }

        Box(
            modifier = modifier
                .background(PreviewLabTheme.colors.background)
                .fillMaxSize(),
        ) {
            ListDetailScaffold(
                list = {
                    Row(
                        modifier = Modifier
                            .background(PreviewLabTheme.colors.background)
                            .zIndex(2f),
                    ) {
                        PreviewListTree(
                            previews = previewList,
                            isSelected = { it == state.selectedPreview },
                            onSelect = { state.select(it) },
                        )
                        Divider()
                    }
                },
                selectedItem = state.selectedPreview,
                detail = { selectedPreview ->
                    CompositionLocalProvider(
                        LocalCollectedPreview provides selectedPreview,
                    ) {
                        AnimatedContent(
                            targetState = selectedPreview,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            modifier = Modifier
                                .zIndex(0f),
                        ) { selectedPreview ->
                            val preview = selectedPreview.content

                            Box(
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                if (previewList.isEmpty()) {
                                    NoPreview()
                                } else {
                                    preview.invoke()
                                }
                            }
                        }
                    }
                },
                detailPlaceholder = { NoSelectedPreview() },
                onUnselect = { state.unselect() },
                listMaxWidth = 200.dp,
            )
        }
    }
}

internal val LocalCollectedPreview = compositionLocalOf<CollectedPreview?> { null }

@Composable
internal fun <Item : Any> ListDetailScaffold(
    list: @Composable () -> Unit,
    selectedItem: Item?,
    detail: @Composable (Item) -> Unit,
    detailPlaceholder: @Composable () -> Unit = { },
    onUnselect: () -> Unit,
    modifier: Modifier = Modifier,
    listMaxWidth: Dp = 200.dp,
) {
    val listContent = remember { movableContentOf { list() } }
    val detailContent = remember { movableContentOf { item: Item -> detail(item) } }

    adaptive(
        small = {
            var openBottomSheet by remember { mutableStateOf(false) }

            LaunchedEffect(selectedItem) {
                openBottomSheet = selectedItem != null
            }

            Box {
                listContent()

                SimpleBottomSheet(
                    isVisible = openBottomSheet,
                    onDismissRequest = { onUnselect() },
                ) {
                    if (selectedItem != null) {
                        detailContent(selectedItem)
                    } else {
                        detailPlaceholder()
                    }
                }
            }
        },
        medium = {
            Row(
                modifier = modifier.fillMaxSize(),
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = adaptive(Dp.Infinity, listMaxWidth))
                        .fillMaxHeight()
                        .zIndex(2f),
                ) {
                    listContent()
                }

                if (selectedItem != null) {
                    detailContent(selectedItem)
                } else {
                    detailPlaceholder()
                }
            }
        },
    )
}
