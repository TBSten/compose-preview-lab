package me.tbsten.compose.preview.lab

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import me.tbsten.compose.preview.lab.component.CommonIconButton
import me.tbsten.compose.preview.lab.component.Divider
import me.tbsten.compose.preview.lab.component.NoPreview
import me.tbsten.compose.preview.lab.component.NoSelectedPreview
import me.tbsten.compose.preview.lab.component.SimpleModal
import me.tbsten.compose.preview.lab.component.adaptive
import me.tbsten.compose.preview.lab.core.generated.resources.Res
import me.tbsten.compose.preview.lab.core.generated.resources.icon_remove
import me.tbsten.compose.preview.lab.openfilehandler.LocalOpenFileHandler
import me.tbsten.compose.preview.lab.openfilehandler.OpenFileHandler
import me.tbsten.compose.preview.lab.previewlist.PreviewListTree
import me.tbsten.compose.preview.lab.previewlist.SearchBar
import me.tbsten.compose.preview.lab.previewlist.filterByQuery
import me.tbsten.compose.preview.lab.previewlist.groupingByFeaturedFiles
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.HorizontalDivider
import me.tbsten.compose.preview.lab.ui.components.Text
import me.tbsten.compose.preview.lab.ui.components.VerticalDivider
import org.jetbrains.compose.resources.painterResource

/**
 * A Composable function that catalogs and displays a list of Previews. The left sidebar actually displays the list of Previews, and the selected Preview is displayed in the center of the screen.
 *
 * @param previewList CollectedPreviews collected from gradle plugins, etc. Note that CollectedPreviews not specified here will not be displayed.
 * @param modifier Modifier to be applied to the root layout of the PreviewLabGallery.
 * @param state [PreviewLabGalleryState] to manage the state of the PreviewLabGallery. Preserves the state of the selected Preview, etc. By default, remember is used (i.e., the composition of the call to Composable is the scope of the state), but the scope (storage period) of the state can be adjusted by moving it to a state holder, such as ViewModel, if necessary.
 * @param openFileHandler By specifying OpenFileHandler, you can display a "Source Code" button that displays the source code corresponding to the Preview.
 * @param featuredFileList Map of group names to file paths for organizing previews into featured categories. Files matching these paths will be grouped under their respective category names in the preview list.
 *
 * @see PreviewLabPreview
 * @see OpenFileHandler
 */
@Composable
fun PreviewLabGallery(
    previewList: List<PreviewLabPreview>,
    modifier: Modifier = Modifier,
    state: PreviewLabGalleryState = remember { PreviewLabGalleryState() },
    openFileHandler: OpenFileHandler<out Any?>? = null,
    featuredFileList: Map<String, List<String>> = emptyMap(),
) = PreviewLabTheme {
    val groupedPreviews by remember(previewList, featuredFileList) {
        derivedStateOf {
            previewList.groupingByFeaturedFiles(featuredFileList) +
                ("all" to previewList)
        }
    }

    val previewLabGalleryNavigator =
        rememberPreviewLabGalleryNavigator(
            state = state,
            groupedPreviews = groupedPreviews,
        )

    val background = PreviewLabTheme.colors.background

    CompositionLocalProvider(
        LocalOpenFileHandler provides openFileHandler,
        LocalPreviewLabGalleryNavigator provides previewLabGalleryNavigator,
    ) {
        Box(
            modifier = modifier
                .background(background)
                .fillMaxSize(),
        ) {
            ListDetailScaffold(
                list = {
                    Row(
                        modifier = Modifier
                            .background(background)
                            .zIndex(2f),
                    ) {
                        LazyColumn {
                            stickyHeader {
                                SearchBar(
                                    query = state.query,
                                    onQueryChange = state::onQueryChange,
                                    modifier = Modifier
                                        .background(background),
                                )
                                HorizontalDivider()
                            }

                            groupedPreviews.entries.forEachIndexed { index, (groupName, previews) ->
                                val filteredPreviews = previews.filterByQuery(state.query)

                                item {
                                    SelectionContainer {
                                        Text(
                                            text = buildAnnotatedString {
                                                append(groupName)
                                                withStyle(PreviewLabTheme.typography.label3.toSpanStyle()) {
                                                    append(" ")
                                                    append("(")
                                                    append("${filteredPreviews.size}")
                                                    append(")")
                                                }
                                            },
                                            color = PreviewLabTheme.colors.textSecondary,
                                            style = PreviewLabTheme.typography.label2,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .background(PreviewLabTheme.colors.background)
                                                .padding(4.dp)
                                                .fillMaxWidth(),
                                        )
                                    }
                                }
                                item {
                                    PreviewListTree(
                                        previews = filteredPreviews,
                                        canAddToComparePanel = state.canAddToComparePanel,
                                        isSelected = {
                                            groupName == state.selectedPreview?.groupName &&
                                                it == state.selectedPreview?.preview
                                        },
                                        onSelect = {
                                            state.select(groupName, it)
                                        },
                                        onAddToComparePanel = {
                                            state.addToComparePanel(groupName, it)
                                        },
                                    )
                                }

                                if (index != groupedPreviews.entries.size - 1) {
                                    item { HorizontalDivider() }
                                }
                            }
                        }
                        Divider()
                    }
                },
                selectedItem = state.selectedPreview,
                detail = { selectedPreview ->
                    Row(modifier = Modifier.fillMaxSize()) {
                        state.selectedPreviews.forEachIndexed { index, selected ->
                            val title = selected.title
                            val preview = selected.preview

                            Column(
                                modifier = Modifier
                                    .background(PreviewLabTheme.colors.background)
                                    .weight(1f),
                            ) {
                                adaptive(
                                    small = {},
                                    medium = {
                                        Column {
                                            Row(
                                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                            ) {
                                                Text(
                                                    text = title,
                                                    style = PreviewLabTheme.typography.body2,
                                                    maxLines = 3,
                                                    overflow = TextOverflow.MiddleEllipsis,
                                                    modifier = Modifier.weight(1f),
                                                )

                                                CommonIconButton(
                                                    painter = painterResource(Res.drawable.icon_remove),
                                                    contentDescription = "Remove $title",
                                                    onClick = { state.removeFromComparePanel(index) },
                                                    enabled = index != 0,
                                                )
                                            }

                                            Divider()
                                        }
                                    },
                                )

                                CompositionLocalProvider(
                                    LocalPreviewLabPreview provides preview,
                                ) {
                                    AnimatedContent(
                                        targetState = preview,
                                        transitionSpec = {
                                            fadeIn() togetherWith fadeOut()
                                        },
                                        modifier = Modifier
                                            .background(PreviewLabTheme.colors.background)
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
                            }
                            if (index != state.selectedPreviews.lastIndex) {
                                VerticalDivider()
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

internal val LocalPreviewLabPreview = compositionLocalOf<PreviewLabPreview?> { null }

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
//    val listContent = remember { movableContentOf { list() } }
//    val detailContent = remember { movableContentOf { item: Item -> detail(item) } }

    adaptive(
        small = {
            var openBottomSheet by remember { mutableStateOf(false) }

            LaunchedEffect(selectedItem) {
                openBottomSheet = selectedItem != null
            }

            Box {
                list()

                SimpleModal(
                    isVisible = openBottomSheet,
                    onDismissRequest = { onUnselect() },
                ) {
                    if (selectedItem != null) {
                        detail(selectedItem)
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
                    list()
                }

                if (selectedItem != null) {
                    detail(selectedItem)
                } else {
                    detailPlaceholder()
                }
            }
        },
    )
}
