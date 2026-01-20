package me.tbsten.compose.preview.lab.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import me.tbsten.compose.preview.lab.PreviewLabPreview
import kotlin.js.ExperimentalJsExport
import me.tbsten.compose.preview.lab.util.JsOnlyExport

@OptIn(ExperimentalJsExport::class)
@JsOnlyExport
public interface PreviewLabGalleryNavigator {
    public fun navigate(id: String)
    public fun back()
}

@OptIn(ExperimentalJsExport::class)
@JsOnlyExport
public object NoOpPreviewLabGalleryNavigator : PreviewLabGalleryNavigator {
    public override fun navigate(id: String) {}

    public override fun back() {}
}

public inline fun PreviewLabGalleryNavigator?.navigateOr(id: String, fallback: () -> Unit): Unit = if (this != null) {
    navigate(id = id)
} else {
    fallback()
}

public val LocalPreviewLabGalleryNavigator: ProvidableCompositionLocal<PreviewLabGalleryNavigator?> =
    staticCompositionLocalOf<PreviewLabGalleryNavigator?> { null }

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
