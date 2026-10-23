/*
 * Copyright (c) 2026 Composable Horizons — MIT License (see ui/THIRD_PARTY_NOTICES.md).
 * Adapted for Compose Preview Lab: PreviewLab* prefix, compose-unstyled 2.6.0, PreviewLabTheme facade colours.
 */
package me.tbsten.compose.preview.lab.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.UnstyledHorizontalSeparator
import com.composeunstyled.UnstyledVerticalSeparator
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme

/**
 * A horizontal divider line (fills the available width).
 *
 * @param modifier Modifier applied to the separator.
 * @param color Colour used for the separator line.
 * @param thickness Thickness of the separator line.
 */
@Composable
@UiComposePreviewLabApi
fun PreviewLabHorizontalSeparator(
    modifier: Modifier = Modifier,
    color: Color = PreviewLabTheme.colors.outline,
    thickness: Dp = 1.dp,
) {
    UnstyledHorizontalSeparator(
        color = color,
        modifier = modifier.clip(RoundedCornerShape(100)),
        thickness = thickness,
    )
}

/**
 * A vertical divider line (fills the available height).
 *
 * @param modifier Modifier applied to the separator.
 * @param color Colour used for the separator line.
 * @param thickness Thickness of the separator line.
 */
@Composable
@UiComposePreviewLabApi
fun PreviewLabVerticalSeparator(
    modifier: Modifier = Modifier,
    color: Color = PreviewLabTheme.colors.outline,
    thickness: Dp = 1.dp,
) {
    UnstyledVerticalSeparator(
        color = color,
        modifier = modifier.clip(RoundedCornerShape(100)),
        thickness = thickness,
    )
}

/**
 * Scope-dispatched alias: inside a [ColumnScope] a divider is horizontal.
 * Delegates to [PreviewLabHorizontalSeparator].
 */
@Composable
@UiComposePreviewLabApi
fun ColumnScope.PreviewLabDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = PreviewLabTheme.colors.outline,
) = PreviewLabHorizontalSeparator(
    modifier = modifier,
    color = color,
    thickness = thickness,
)

/**
 * Scope-dispatched alias: inside a [RowScope] a divider is vertical.
 * Delegates to [PreviewLabVerticalSeparator].
 */
@Composable
@UiComposePreviewLabApi
fun RowScope.PreviewLabDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = PreviewLabTheme.colors.outline,
) = PreviewLabVerticalSeparator(
    modifier = modifier,
    color = color,
    thickness = thickness,
)
