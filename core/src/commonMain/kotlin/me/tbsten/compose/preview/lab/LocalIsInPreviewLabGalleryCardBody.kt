package me.tbsten.compose.preview.lab

import androidx.compose.runtime.compositionLocalOf

/**
 * `true` when the current composition is inside a PreviewLabGallery card body. Used by
 * `PreviewLab` to skip its full UI (controls, inspectors, …) and render only the preview
 * content, producing the scaled-down thumbnails the gallery displays.
 *
 * Read this CompositionLocal from your composable if you want to render a gallery-only
 * label / explanatory image instead of the live content.
 */
val LocalIsInPreviewLabGalleryCardBody = compositionLocalOf { false }
