/*
 * Copyright (c) 2026 Composable Horizons — MIT License (see ui/THIRD_PARTY_NOTICES.md).
 * Adapted for Compose Preview Lab: PreviewLab* prefix, compose-unstyled 2.6.0, PreviewLabTheme facade colours.
 */
package me.tbsten.compose.preview.lab.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import com.composeunstyled.LocalContentColor
import com.composeunstyled.UnstyledIcon
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi

/**
 * An [ImageVector] icon tinted with the current content colour by default.
 *
 * @param imageVector Vector asset drawn by the icon.
 * @param modifier Modifier applied to the icon.
 * @param contentDescription Accessible description announced for the icon.
 * @param tint Tint colour applied to the icon; [Color.Unspecified] draws the asset untinted.
 */
@Composable
@UiComposePreviewLabApi
fun PreviewLabIcon(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = LocalContentColor.current,
) {
    UnstyledIcon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
    )
}

/**
 * An [ImageBitmap] icon tinted with the current content colour by default.
 *
 * @param bitmap Bitmap drawn by the icon.
 * @param modifier Modifier applied to the icon.
 * @param contentDescription Accessible description announced for the icon.
 * @param tint Tint colour applied to the icon; [Color.Unspecified] draws the asset untinted.
 */
@Composable
@UiComposePreviewLabApi
fun PreviewLabIcon(
    bitmap: ImageBitmap,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = LocalContentColor.current,
) {
    UnstyledIcon(
        imageBitmap = bitmap,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
    )
}

/**
 * A [Painter] icon tinted with the current content colour by default.
 *
 * @param painter Painter drawn by the icon.
 * @param modifier Modifier applied to the icon.
 * @param contentDescription Accessible description announced for the icon.
 * @param tint Tint colour applied to the icon; [Color.Unspecified] draws the asset untinted.
 */
@Composable
@UiComposePreviewLabApi
fun PreviewLabIcon(
    painter: Painter,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = LocalContentColor.current,
) {
    UnstyledIcon(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
    )
}
