package me.tbsten.compose.preview.lab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

interface PreviewLabRootNavigator {
    fun navigate(id: String)
    fun back()
}

inline fun PreviewLabRootNavigator?.navigateOr(id: String, fallback: () -> Unit) = if (this != null) {
    navigate(id = id)
} else {
    fallback()
}

val LocalPreviewLabRootNavigator = staticCompositionLocalOf<PreviewLabRootNavigator?> { null }

@Composable
private fun rememberPreviewLabRootNavigator(onNavigatePreview: (String) -> Unit, onBack: () -> Unit) = remember {
    object : PreviewLabRootNavigator {
        override fun navigate(id: String) {
            onNavigatePreview(id)
        }

        override fun back() {
            onBack()
        }
    }
}

@Composable
internal fun rememberPreviewLabRootNavigator(
    state: PreviewLabRootState,
    groupedPreviews: Map<String, List<CollectedPreview>>,
) = rememberPreviewLabRootNavigator(
    onNavigatePreview = { id ->
        val target = groupedPreviews.entries.firstNotNullOfOrNull { (groupName, previewsInGroup) ->
            previewsInGroup
                .firstOrNull { preview -> preview.id == id }
                ?.let { groupName to it }
        }
        target?.let { (groupName, preview) ->
            state.select(groupName = groupName, preview = preview)
        }
    },
    onBack = { state.unselect() },
)
