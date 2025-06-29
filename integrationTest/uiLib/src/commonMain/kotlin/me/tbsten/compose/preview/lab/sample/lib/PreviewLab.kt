package me.tbsten.compose.preview.lab.sample.lib

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.PreviewLabState
import me.tbsten.compose.preview.lab.field.ScreenSize

val customizedPreviewLab = PreviewLab(
    defaultState = { remember { PreviewLabState() } },
    defaultScreenSizes = ScreenSize.AllPresets,
    contentRoot = { content ->
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = Color.Red,
                onPrimary = Color.Yellow,
            ),
        ) {
            content()
        }
    },
)
