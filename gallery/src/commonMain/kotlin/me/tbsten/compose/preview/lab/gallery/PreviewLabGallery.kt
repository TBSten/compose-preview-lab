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
import me.tbsten.compose.preview.lab.LocalPreviewLabPreview
import me.tbsten.compose.preview.lab.PreviewLabPreview
import me.tbsten.compose.preview.lab.gallery.previewlist.PreviewListTree
import me.tbsten.compose.preview.lab.gallery.previewlist.SearchBar
import me.tbsten.compose.preview.lab.gallery.previewlist.filterByQuery
import me.tbsten.compose.preview.lab.gallery.previewlist.groupingByFeaturedFiles
import me.tbsten.compose.preview.lab.previewlab.openfilehandler.LocalOpenFileHandler
import me.tbsten.compose.preview.lab.previewlab.openfilehandler.OpenFileHandler
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.adaptive
import me.tbsten.compose.preview.lab.ui.components.PreviewLabDivider
import me.tbsten.compose.preview.lab.ui.components.PreviewLabHorizontalDivider
import me.tbsten.compose.preview.lab.ui.components.PreviewLabIconButton
import me.tbsten.compose.preview.lab.ui.components.PreviewLabText
import me.tbsten.compose.preview.lab.ui.generated.resources.PreviewLabUiRes
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_remove
import org.jetbrains.compose.resources.painterResource

/**
 * Catch-all group that bundles every preview regardless of `featuredFileList` categorisation.
 */
const val AllGroupName = "all"

/**
 * Catalogues a list of previews: list in the left sidebar, selected preview in the centre.
 *
 * @param previewList Previews to display. Only entries listed here surface in the gallery.
 * @param state Defaults to `remember`; lift to a ViewModel etc. to outlive composition.
 * @param openFileHandler Pass an [OpenFileHandler] to render a "Source Code" jump button.
 * @param featuredFileList Group name → file path map for organising previews into named
 *   categories; matching files are grouped under their category name in the sidebar.
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

                            PreviewLabDivider(
                                color = PreviewLabTheme.colors.outline,
                                modifier = Modifier.zIndex(9999f).fillMaxHeight(),
                            )
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
                                PreviewLabDivider()
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
            PreviewLabHorizontalDivider()
        }

        groupedPreviews.entries.forEachIndexed { index, (groupName, previews) ->
            val filteredPreviews = previews.filterByQuery(state.query)

            item {
                SelectionContainer {
                    PreviewLabText(
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
                item { PreviewLabHorizontalDivider() }
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

@Composable
private fun SelectedPreviewTitleHeader(selectedPreview: SelectedPreview, onRemoveClick: () -> Unit) = Column {
    Row(
        modifier = Modifier.padding(12.dp).fillMaxWidth(),
    ) {
        PreviewLabText(
            text = selectedPreview.title,
            style = PreviewLabTheme.typography.body2,
            maxLines = 3,
            overflow = TextOverflow.MiddleEllipsis,
            modifier = Modifier.weight(1f),
        )

        PreviewLabIconButton(
            painter = painterResource(PreviewLabUiRes.drawable.icon_remove),
            contentDescription = "Remove ${selectedPreview.title}",
            onClick = {
                onRemoveClick()
            },
        )
    }

    PreviewLabDivider()
}
