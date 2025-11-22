package me.tbsten.compose.preview.lab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

interface PreviewLabGalleryNavigator {
    fun navigate(id: String)
    fun back()
}

inline fun PreviewLabGalleryNavigator?.navigateOr(id: String, fallback: () -> Unit) = if (this != null) {
    navigate(id = id)
} else {
    fallback()
}

val LocalPreviewLabGalleryNavigator = staticCompositionLocalOf<PreviewLabGalleryNavigator?> { null }

@Composable
private fun rememberPreviewLabGalleryNavigator(onNavigatePreview: (String) -> Unit, onBack: () -> Unit) = remember {
    object : PreviewLabGalleryNavigator {
        override fun navigate(id: String) {
            onNavigatePreview(id)
        }

        override fun back() {
            onBack()
        }
    }
}

@Composable
internal fun rememberPreviewLabGalleryNavigator(
    state: PreviewLabGalleryState,
    groupedPreviews: Map<String, List<PreviewLabPreview>>,
) = remember {
    object : PreviewLabGalleryNavigator {
        override fun navigate(id: String) {
            val target = groupedPreviews.entries.firstNotNullOfOrNull { (groupName, previewsInGroup) ->
                previewsInGroup
                    .firstOrNull { preview -> preview.id == id }
                    ?.let { groupName to it }
            }
            target?.let { (groupName, preview) ->
                state.select(groupName = groupName, preview = preview)
            }
        }

        override fun back() {
            state.unselect()
        }
    }
}
