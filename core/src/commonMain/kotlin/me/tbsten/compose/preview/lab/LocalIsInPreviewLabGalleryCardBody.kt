package me.tbsten.compose.preview.lab

import androidx.compose.runtime.compositionLocalOf

/**
 * CompositionLocal that indicates whether the current composition is inside a PreviewLabGallery card body.
 * When true, PreviewLab will skip rendering its full UI and only render the preview content directly.
 * This is used to show scaled-down preview thumbnails in the gallery view.
 *
 * Using this CompositionLocal, you can display simple explanatory images instead of content in the PreviewLabGallery, or implement labels that appear only on the Gallery.
 */
val LocalIsInPreviewLabGalleryCardBody = compositionLocalOf { false }
