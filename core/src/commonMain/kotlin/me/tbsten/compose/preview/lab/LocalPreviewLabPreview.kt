package me.tbsten.compose.preview.lab

import androidx.compose.runtime.compositionLocalOf

/**
 * CompositionLocal that provides the current [PreviewLabPreview] being displayed.
 * This is set by PreviewLabGallery when a preview is selected.
 */
val LocalPreviewLabPreview = compositionLocalOf<PreviewLabPreview?> { null }
