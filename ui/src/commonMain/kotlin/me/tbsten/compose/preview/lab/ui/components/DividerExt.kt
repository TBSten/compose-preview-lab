package me.tbsten.compose.preview.lab.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi

@Composable
@InternalComposePreviewLabApi
fun ColumnScope.PreviewLabDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = PreviewLabDividerDefaults.Thickness,
    color: Color = PreviewLabDividerDefaults.color,
) = PreviewLabHorizontalDivider(
    modifier = modifier,
    thickness = thickness,
    color = color,
)

@Composable
@InternalComposePreviewLabApi
fun RowScope.PreviewLabDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = PreviewLabDividerDefaults.Thickness,
    color: Color = PreviewLabDividerDefaults.color,
) = PreviewLabVerticalDivider(
    modifier = modifier,
    thickness = thickness,
    color = color,
)
