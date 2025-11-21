package me.tbsten.compose.preview.lab.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import me.tbsten.compose.preview.lab.PreviewLabGalleryState
import me.tbsten.compose.preview.lab.PreviewLabPreview

@Composable
fun rememberPreviewLabGalleryState(initialGroupName: String, initialPreview: PreviewLabPreview) =
    remember { PreviewLabGalleryState() }.also { galleryState ->
        LaunchedEffect(galleryState) {
            galleryState.select(
                groupName = initialGroupName,
                preview = initialPreview,
            )
        }
    }
