package me.tbsten.compose.preview.lab.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import me.tbsten.compose.preview.lab.ui.components.DividerDefaults
import me.tbsten.compose.preview.lab.ui.components.HorizontalDivider
import me.tbsten.compose.preview.lab.ui.components.VerticalDivider

@Composable
internal fun ColumnScope.Divider(
    modifier: Modifier = Modifier,
    thickness: Dp = DividerDefaults.Thickness,
    color: Color = DividerDefaults.color,
) = HorizontalDivider(
    modifier = modifier,
    thickness = thickness,
    color = color,
)

@Composable
internal fun RowScope.Divider(
    modifier: Modifier = Modifier,
    thickness: Dp = DividerDefaults.Thickness,
    color: Color = DividerDefaults.color,
) = VerticalDivider(
    modifier = modifier,
    thickness = thickness,
    color = color,
)
