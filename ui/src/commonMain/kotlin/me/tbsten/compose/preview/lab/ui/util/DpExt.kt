package me.tbsten.compose.preview.lab.ui.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi

@UiComposePreviewLabApi
fun Offset.toDpOffset(density: Density): DpOffset = with(density) {
    DpOffset(
        x = x.toDp(),
        y = y.toDp(),
    )
}

@UiComposePreviewLabApi
fun IntSize.toDpSize(density: Density): DpSize = with(density) {
    DpSize(
        width = width.toDp(),
        height = height.toDp(),
    )
}
