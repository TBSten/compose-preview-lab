package me.tbsten.compose.preview.lab.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import kotlin.math.min
import kotlin.math.roundToInt
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.LocalIsInPreviewLabGalleryCardBody
import me.tbsten.compose.preview.lab.PreviewLabPreview
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.adaptive
import me.tbsten.compose.preview.lab.ui.components.PreviewLabHorizontalDivider
import me.tbsten.compose.preview.lab.ui.components.PreviewLabText
import me.tbsten.compose.preview.lab.ui.components.card.PreviewLabCardDefaults
import me.tbsten.compose.preview.lab.ui.components.card.PreviewLabOutlinedCard

@Composable
fun PreviewListGrid(
    groupedPreviewList: Map<String, List<PreviewLabPreview>>,
    onPreviewClick: (String, PreviewLabPreview) -> Unit,
    contentPadding: PaddingValues = PaddingValues.Zero,
) = SelectionContainer {
    val columnWidth = 200.dp
    val maxColumn = 5
    val itemSpacing = adaptive(12.dp, 20.dp)

    LazyVerticalGrid(
        columns = GridCells.Adaptive(columnWidth).max(columnWidth * maxColumn + itemSpacing * (maxColumn - 1)),
        horizontalArrangement = Arrangement.spacedBy(itemSpacing, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(itemSpacing),
        contentPadding = contentPadding,
    ) {
        previewListGrid(
            key = "previewList",
            groupedPreviewList = groupedPreviewList,
            onPreviewClick = onPreviewClick,
        )
    }
}

private fun LazyGridScope.previewListGrid(
    key: Any?,
    groupedPreviewList: Map<String, List<PreviewLabPreview>>,
    onPreviewClick: (String, PreviewLabPreview) -> Unit,
) {
    groupedPreviewList.forEach { (groupName, previewList) ->
        item(span = { GridItemSpan(maxLineSpan) }, key = key to "$groupName:header") {
            PreviewLabText(
                text = buildAnnotatedString {
                    append(groupName)
                    withStyle(SpanStyle(fontWeight = FontWeight.Normal, fontSize = 0.8.em)) {
                        append(" (${previewList.size})")
                    }
                },
                style = PreviewLabTheme.typography.h2,
                color = PreviewLabTheme.colors.textSecondary,
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
            )
        }

        if (previewList.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }, key = key to "$groupName:empty") {
                PreviewLabText(
                    text = "(No @Preview in `$groupName` group)",
                    style = PreviewLabTheme.typography.body2,
                    modifier = Modifier.padding(vertical = 20.dp),
                )
            }
        }

        items(previewList, key = { Triple(key, "$groupName:item", it.id) }) { preview ->
            PreviewListGridCard(
                preview = preview,
                onClick = { onPreviewClick(groupName, preview) },
            )
        }
    }
}

@ExperimentalComposePreviewLabApi
@Composable
fun PreviewListGridCard(preview: PreviewLabPreview, onClick: () -> Unit) = PreviewLabOutlinedCard(
    border = PreviewLabCardDefaults.outlinedCardBorder(),
    onClick = onClick,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .padding(8.dp),
    ) {
        val segments = preview.displayName.split(".")
        val groupName = if (segments.size >= 2) segments.dropLast(1).joinToString(".") else null
        val name = segments.last()

        groupName?.let {
            PreviewLabText(
                text = it,
                style = PreviewLabTheme.typography.label3,
                color = PreviewLabTheme.colors.textSecondary,
                minLines = 1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        PreviewLabText(
            text = name,
            style = PreviewLabTheme.typography.label1,
            minLines = 1,
            maxLines = if (groupName == null) 2 else 1,
            overflow = TextOverflow.Ellipsis,
        )
    }

    PreviewLabHorizontalDivider(color = PreviewLabTheme.colors.outline)

    val bodyContentPadding = adaptive(small = 4.dp, medium = 8.dp)
    PreviewLabGalleryCardBody(
        preview = preview,
        modifier = Modifier
            .padding(top = bodyContentPadding)
            .padding(horizontal = bodyContentPadding),
    )
}

@Composable
private fun PreviewLabGalleryCardBody(preview: PreviewLabPreview, modifier: Modifier = Modifier) {
    val scale = adaptive(small = 0.3f, medium = 0.4f)
    val aspectRatio = adaptive(small = 16f / 9f, large = 4f / 3f)

    Box(modifier = modifier) {
        Layout(
            content = {
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            transformOrigin = TransformOrigin(0f, 0f)
                        },
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(aspectRatio),
                    ) {
                        CompositionLocalProvider(
                            LocalIsInPreviewLabGalleryCardBody provides true,
                        ) {
                            preview.content()
                        }
                    }

                    // overlay for disable all events
                    Box(
                        modifier = Modifier
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        awaitPointerEvent(PointerEventPass.Final).changes.forEach { it.consume() }
                                    }
                                }
                            }.fillMaxSize(),
                    )
                }
            },
            measurePolicy = { measurables, constraints ->
                val measurable = measurables.single()
                val placeable = measurable.measure(
                    constraints.copy(
                        maxWidth = (constraints.maxWidth / scale).roundToInt(),
                        minWidth = (constraints.minWidth / scale).roundToInt(),
                        maxHeight = ((constraints.maxWidth / aspectRatio) / scale).roundToInt(),
                        minHeight = (constraints.minWidth / aspectRatio / scale).roundToInt(),
                    ),
                )

                layout(constraints.maxWidth, (constraints.maxWidth / aspectRatio).roundToInt()) {
                    placeable.place(0, 0)
                }
            },
            modifier = Modifier
                .fillMaxWidth(),
        )
    }
}

private fun GridCells.max(max: Dp) = object : GridCells {
    override fun Density.calculateCrossAxisCellSizes(availableSize: Int, spacing: Int): List<Int> {
        val limitedWidth = min(max.toPx(), availableSize.toFloat())

        return with(this@max) {
            calculateCrossAxisCellSizes(limitedWidth.roundToInt(), spacing)
        }
    }
}
