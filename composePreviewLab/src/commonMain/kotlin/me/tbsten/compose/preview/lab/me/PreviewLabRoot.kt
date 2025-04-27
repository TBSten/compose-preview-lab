package me.tbsten.compose.preview.lab.me

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import me.tbsten.compose.preview.lab.me.component.NoPreview
import me.tbsten.compose.preview.lab.me.component.PreviewListItem
import me.tbsten.compose.preview.lab.me.theme.AppTheme

@Composable
fun PreviewLabRoot(
    previews: Sequence<Pair<String, @Composable () -> Unit>>,
    openFileHandler: OpenFileHandler? = null,
) = AppTheme {
    CompositionLocalProvider(
        LocalOpenFileHandler provides openFileHandler,
    ) {
        val previewList = remember { previews.toList() }
        var selectedIndex by remember { mutableStateOf(0) }

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
                ) {
                    previewList.forEachIndexed { index, (title, _) ->
                        PreviewListItem(
                            title = title,
                            isSelected = index == selectedIndex,
                            onSelect = {
                                selectedIndex = index
                            },
                        )
                    }
                }
                VerticalDivider(
                    modifier = Modifier
                        .zIndex(2f)
                )

                AnimatedContent(
                    targetState = selectedIndex,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    modifier = Modifier
                        .zIndex(0f)
                ) { selectedIndex ->
                    val preview = previewList.getOrNull(selectedIndex)?.second

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
