package me.tbsten.compose.preview.lab.sample.lib

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import me.tbsten.compose.preview.lab.field.ScreenSize
import me.tbsten.compose.preview.lab.previewlab.PreviewLab
import me.tbsten.compose.preview.lab.previewlab.PreviewLabScope
import me.tbsten.compose.preview.lab.previewlab.PreviewLabState

@Composable
fun CustomizedPreviewLab(
    modifier: Modifier = Modifier,
    content: @Composable PreviewLabScope.() -> Unit,
) = PreviewLab(
    modifier = modifier,
    state = remember { PreviewLabState() },
    screenSizes = ScreenSize.AllPresets,
    contentRoot = { c ->
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = Color.Red,
                onPrimary = Color.Yellow,
            ),
        ) {
            c()
        }
    },
    content = content,
)
