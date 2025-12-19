package me.tbsten.compose.preview.lab.action

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabAction
import me.tbsten.compose.preview.lab.PreviewLabField
import me.tbsten.compose.preview.lab.action.component.DetailDialogContent
import me.tbsten.compose.preview.lab.action.component.LatestResultView
import me.tbsten.compose.preview.lab.action.component.NoResultsView
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Button
import me.tbsten.compose.preview.lab.ui.components.ButtonVariant
import me.tbsten.compose.preview.lab.ui.components.HorizontalDivider
import me.tbsten.compose.preview.lab.ui.components.SimpleModal
import me.tbsten.compose.preview.lab.ui.components.Text
import me.tbsten.compose.preview.lab.ui.components.card.ElevatedCard

@ExperimentalComposePreviewLabApi
fun <R> defaultResultsViewField(): PreviewLabAction<R>.(R) -> PreviewLabField<R>? = { null }

@ExperimentalComposePreviewLabApi
@Composable
fun <R> PreviewLabAction<R>.DefaultResultsView(
    field: PreviewLabAction<R>.(R) -> PreviewLabField<R>? = defaultResultsViewField(),
    modifier: Modifier = Modifier,
) {
    val latestStatus by remember { derivedStateOf { doActionStatusList.entries.firstOrNull()?.value } }
    var showDetailDialog by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = modifier,
    ) {
        LazyColumn {
            item {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            PreviewLabTheme.typography.h4.toSpanStyle()
                                .copy(fontWeight = FontWeight.Normal, fontSize = 0.8.em),
                        ) {
                            append("Latest")
                            append(" ")
                        }
                        append(label)
                    },
                    style = PreviewLabTheme.typography.h3,
                    modifier = Modifier.padding(8.dp),
                )
            }

            item {
                HorizontalDivider()
            }

            item {
                AnimatedContent(
                    targetState = latestStatus,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut() using SizeTransform(clip = false)
                    },
                ) { latestStatus ->
                    if (latestStatus != null) {
                        LatestResultView(
                            latestStatus = latestStatus,
                            field = field,
                        )
                    } else {
                        NoResultsView(label = label)
                    }
                }
            }

            item {
                HorizontalDivider()
            }

            if (latestStatus != null) {
                item {
                    Button(
                        variant = ButtonVariant.PrimaryGhost,
                        text = "View all",
                        onClick = { showDetailDialog = true },
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }
        }
    }

    SimpleModal(
        isVisible = showDetailDialog,
        onDismissRequest = { showDetailDialog = false },
    ) {
        SelectionContainer {
            DetailDialogContent(
                doActionStatusList = doActionStatusList,
                field = field,
            )
        }
    }
}
