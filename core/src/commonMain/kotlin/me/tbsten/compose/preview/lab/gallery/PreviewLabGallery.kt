package me.tbsten.compose.preview.lab.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabPreview
import me.tbsten.compose.preview.lab.core.generated.resources.Res
import me.tbsten.compose.preview.lab.core.generated.resources.icon_remove
import me.tbsten.compose.preview.lab.gallery.previewlist.PreviewListTree
import me.tbsten.compose.preview.lab.gallery.previewlist.SearchBar
import me.tbsten.compose.preview.lab.gallery.previewlist.filterByQuery
import me.tbsten.compose.preview.lab.gallery.previewlist.groupingByFeaturedFiles
import me.tbsten.compose.preview.lab.openfilehandler.LocalOpenFileHandler
import me.tbsten.compose.preview.lab.openfilehandler.OpenFileHandler
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.adaptive
import me.tbsten.compose.preview.lab.ui.components.CommonIconButton
import me.tbsten.compose.preview.lab.ui.components.Divider
import me.tbsten.compose.preview.lab.ui.components.HorizontalDivider
import me.tbsten.compose.preview.lab.ui.components.Text
import org.jetbrains.compose.resources.painterResource

/**
 * The default group name used for showing all previews in the PreviewLabGallery.
 * This group contains all previews regardless of their featured file categorization.
 */
const val AllGroupName = "all"

/**
 * A Composable function that catalogs and displays a list of Previews. The left sidebar actually displays the list of Previews, and the selected Preview is displayed in the center of the screen.
 *
 * @param previewList CollectedPreviews collected from gradle plugins, etc. Note that CollectedPreviews not specified here will not be displayed.
 * @param modifier Modifier to be applied to the root layout of the PreviewLabGallery.
 * @param state [PreviewLabGalleryState] to manage the state of the PreviewLabGallery. Preserves the state of the selected Preview, etc. By default, remember is used (i.e., the composition of the call to Composable is the scope of the state), but the scope (storage period) of the state can be adjusted by moving it to a state holder, such as ViewModel, if necessary.
 * @param openFileHandler By specifying OpenFileHandler, you can display a "Source Code" button that displays the source code corresponding to the Preview.
 * @param featuredFileList Map of group names to file paths for organizing previews into featured categories. Files matching these paths will be grouped under their respective category names in the preview list.
 *
 * @see me.tbsten.compose.preview.lab.PreviewLabPreview
 * @see OpenFileHandler
 */
@Composable
fun PreviewLabGallery(
    previewList: List<PreviewLabPreview>,
    modifier: Modifier = Modifier,
    state: PreviewLabGalleryState = remember { PreviewLabGalleryState() },
    openFileHandler: OpenFileHandler<out Any?>? = null,
    featuredFileList: Map<String, List<String>> = emptyMap(),
    noSelectedContents: @Composable (Map<String, List<PreviewLabPreview>>) -> Unit = { groupedPreviews ->
        PreviewListGrid(
            groupedPreviewList = groupedPreviews,
            onPreviewClick = { group, preview -> state.select(group, preview) },
            contentPadding = PaddingValues(adaptive(12.dp, 20.dp)),
        )
    },
) = PreviewLabTheme {
    val groupedPreviews by remember(previewList, featuredFileList) {
        derivedStateOf {
            previewList.groupingByFeaturedFiles(featuredFileList) +
                (AllGroupName to previewList)
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
            Row {
                // side bar
                adaptive(
                    small = {},
                    medium = {
                        Row(Modifier.width(200.dp)) {
                            Sidebar(
                                groupedPreviews = groupedPreviews,
                                state = state,
                            )

                            Divider(color = PreviewLabTheme.colors.outline, modifier = Modifier.zIndex(9999f).fillMaxHeight())
                        }
                    },
                )

                // selected contents
                val selectedPreviews = state.selectedPreviews
                if (selectedPreviews.isEmpty()) {
                    noSelectedContents(groupedPreviews)
                } else {
                    Row(Modifier.zIndex(-1f)) {
                        selectedPreviews.forEachIndexed { selectedPreviewIndex, selectedPreview ->
                            Sidebar(
                                index = selectedPreviewIndex,
                                selectedPreview = selectedPreview,
                                state = state,
                                modifier = Modifier.weight(1f),
                            )

                            if (selectedPreviewIndex != selectedPreviews.lastIndex) {
                                Divider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Sidebar(groupedPreviews: Map<String, List<PreviewLabPreview>>, state: PreviewLabGalleryState) {
    LazyColumn {
        stickyHeader {
            SearchBar(
                query = state.query,
                onQueryChange = state::onQueryChange,
                modifier = Modifier
                    .background(PreviewLabTheme.colors.background),
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
}

@Composable
private fun Sidebar(
    index: Int,
    selectedPreview: SelectedPreview,
    state: PreviewLabGalleryState,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        SelectedPreviewTitleHeader(
            selectedPreview = selectedPreview,
            onRemoveClick = {
                // TODO refactor so that only calling unselect() is needed
                if (index == 0) {
                    state.unselect()
                } else {
                    state.removeFromComparePanel(index)
                }
            },
        )

        CompositionLocalProvider(
            LocalPreviewLabPreview provides selectedPreview.preview,
        ) {
            selectedPreview.preview.content()
        }
    }
}

@InternalComposePreviewLabApi
val LocalPreviewLabPreview = compositionLocalOf<PreviewLabPreview?> { null }

@Composable
private fun SelectedPreviewTitleHeader(selectedPreview: SelectedPreview, onRemoveClick: () -> Unit) = Column {
    Row(
        modifier = Modifier.padding(12.dp).fillMaxWidth(),
    ) {
        Text(
            text = selectedPreview.title,
            style = PreviewLabTheme.typography.body2,
            maxLines = 3,
            overflow = TextOverflow.MiddleEllipsis,
            modifier = Modifier.weight(1f),
        )

        CommonIconButton(
            painter = painterResource(Res.drawable.icon_remove),
            contentDescription = "Remove ${selectedPreview.title}",
            onClick = {
                onRemoveClick()
            },
        )
    }

    Divider()
}
