package me.tbsten.compose.preview.lab

import androidx.compose.runtime.compositionLocalOf

/**
 * CompositionLocal that provides the current [PreviewLabPreview] being displayed.
 * This is set by PreviewLabGallery when a preview is selected.
 */
public val LocalPreviewLabPreview: androidx.compose.runtime.ProvidableCompositionLocal<PreviewLabPreview?> =
    compositionLocalOf<PreviewLabPreview?> { null }
