package me.tbsten.compose.preview.lab.me

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PreviewLabRoot(
    previews: Sequence<Pair<String, @Composable () -> Unit>>,
    openFileHandler: OpenFileHandler? = null,
) = CompositionLocalProvider(
    LocalOpenFileHandler provides openFileHandler,
) {
    val previewList = remember { previews.toList() }
    var selectedIndex by remember { mutableStateOf(0) }

    Row {
        Column(
            modifier = Modifier
                .width(200.dp)
        ) {
            previewList.forEachIndexed { index, (title, _) ->
                Row(
                    modifier = Modifier
                        .clickable { selectedIndex = index }
                        .padding(16.dp)
                ) {
                    Text(
                        text = title,
                    )
                }
            }
        }
        VerticalDivider()

        AnimatedContent(
            targetState = selectedIndex,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            }
        ) { selectedIndex ->
            val preview = previewList?.getOrNull(selectedIndex)?.second

            preview
                ?.invoke()
                ?: NoPreview()
        }
    }
}

@Composable
private fun NoPreview() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "No preview",
        )
    }
}
