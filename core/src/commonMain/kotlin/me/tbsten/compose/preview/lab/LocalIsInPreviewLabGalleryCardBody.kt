package me.tbsten.compose.preview.lab

import androidx.compose.runtime.compositionLocalOf

/**
 * CompositionLocal that indicates whether the current composition is inside a PreviewLabGallery card body.
 * When true, PreviewLab will skip rendering its full UI and only render the preview content directly.
 * This is used to show scaled-down preview thumbnails in the gallery view.
 */
val LocalIsInPreviewLabGalleryCardBody = compositionLocalOf { false }
