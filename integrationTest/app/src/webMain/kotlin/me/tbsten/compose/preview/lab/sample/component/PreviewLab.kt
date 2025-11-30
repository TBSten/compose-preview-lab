package me.tbsten.compose.preview.lab.sample.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.ScreenSize

internal val previewLab = PreviewLab(
    defaultScreenSizes = listOf(ScreenSize(320.dp, 400.dp)) + ScreenSize.SmartphoneAndDesktops,
    contentRoot = { content ->
        Column(Modifier.padding(16.dp)) {
            content()
        }
    },
)
