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
public fun ColumnScope.Divider(
    modifier: Modifier = Modifier,
    thickness: Dp = DividerDefaults.Thickness,
    color: Color = DividerDefaults.color,
): Unit = HorizontalDivider(
    modifier = modifier,
    thickness = thickness,
    color = color,
)

@Composable
@InternalComposePreviewLabApi
public fun RowScope.Divider(
    modifier: Modifier = Modifier,
    thickness: Dp = DividerDefaults.Thickness,
    color: Color = DividerDefaults.color,
): Unit = VerticalDivider(
    modifier = modifier,
    thickness = thickness,
    color = color,
)
