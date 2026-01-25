package me.tbsten.compose.preview.lab.sample.component

import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.field.ScreenSize
import me.tbsten.compose.preview.lab.previewlab.PreviewLab

val previewLab = PreviewLab(
    defaultScreenSizes = listOf(ScreenSize(320.dp, 400.dp)) + ScreenSize.SmartphoneAndDesktops,
    contentRoot = { content ->
        content()
    },
)
