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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import me.tbsten.compose.preview.lab.component.CommonListItem
import me.tbsten.compose.preview.lab.component.NoPreview
import me.tbsten.compose.preview.lab.component.PreviewGroupList
import me.tbsten.compose.preview.lab.groupByDisplayName
import me.tbsten.compose.preview.lab.theme.AppTheme

@Composable
fun PreviewLabRoot(
    previews: List<CollectedPreview>,
    state: PreviewLabRootState = remember { PreviewLabRootState() },
    openFileHandler: OpenFileHandler? = null,
) = AppTheme {
    CompositionLocalProvider(
        LocalOpenFileHandler provides openFileHandler,
    ) {
        val previewList = remember { previews.toList() }
        val groupedPreviews = remember { previewList.groupByDisplayName() }

        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize(),
        ) {
            Row {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .width(200.dp)
                        .fillMaxHeight()
                        .zIndex(2f)
                        .verticalScroll(rememberScrollState())
                ) {
                    PreviewGroupList(
                        items = groupedPreviews,
                        selectedPreviewIndex = state.selectedPreviewIndex,
                        onPreviewSelect = { index ->
                            state.selectedPreviewIndex = index
                        }
                    )
                }
                VerticalDivider(
                    modifier = Modifier
                        .zIndex(2f)
                )

                AnimatedContent(
                    targetState = state.selectedPreviewIndex,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    modifier = Modifier
                        .zIndex(0f)
                ) { selectedIndex ->
                    val preview = previewList.getOrNull(selectedIndex)?.content

                    Box(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        preview
                            ?.invoke()
                            ?: NoPreview()
                    }
                }
            }
        }
    }
}
