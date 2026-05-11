package me.tbsten.compose.preview.lab

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import java.util.Collections.emptyMap
import me.tbsten.compose.preview.lab.gallery.PreviewLabGallery
import me.tbsten.compose.preview.lab.gallery.PreviewLabGalleryState
import me.tbsten.compose.preview.lab.gallery.PreviewListGrid
import me.tbsten.compose.preview.lab.previewlab.openfilehandler.OpenFileHandler
import me.tbsten.compose.preview.lab.ui.adaptive

/**
 * Hosts [PreviewLabGallery] in a JVM desktop [Window]. Most parameters are forwarded as-is to
 * Compose's [Window] (see its KDoc for window-level semantics).
 *
 * ```kotlin
 * fun main() = application {
 *     PreviewLabGalleryWindows(
 *         previewList = myModule.PreviewList,
 *         openFileHandler = UrlOpenFileHandler("https://github.com/user/repo/blob/main"),
 *         featuredFileList = myModule.FeaturedFileList,
 *         windowState = rememberWindowState(size = DpSize(1400.dp, 900.dp)),
 *         title = "My Component Gallery",
 *     )
 * }
 * ```
 *
 * @param previewList Previews to display.
 * @param openFileHandler Optional handler enabling a "Source Code" jump for each preview.
 * @param featuredFileList Grouping map of file paths by category name.
 * @param state Pull this out to a ViewModel etc. if the gallery state needs to outlive the
 *   composition.
 * @param noSelectedContents Content shown when nothing is selected — defaults to a
 *   [PreviewListGrid] click target.
 * @see me.tbsten.compose.preview.lab.gallery.PreviewLabGallery
 * @see OpenFileHandler
 */
@Composable
fun ApplicationScope.PreviewLabGalleryWindows(
    previewList: List<PreviewLabPreview>,
    openFileHandler: OpenFileHandler<out Any?>? = null,
    featuredFileList: Map<String, List<String>> = emptyMap(),
    state: PreviewLabGalleryState = remember { PreviewLabGalleryState() },
    noSelectedContents: @Composable (Map<String, List<PreviewLabPreview>>) -> Unit = { groupedPreviews ->
        PreviewListGrid(
            groupedPreviewList = groupedPreviews,
            onPreviewClick = { group, preview -> state.select(group, preview) },
            contentPadding = PaddingValues(adaptive(12.dp, 20.dp)),
        )
    },
    // Main window arguments
    // TODO: Review appropriate default values
    windowState: WindowState = rememberWindowState(size = DpSize(1000.dp, 800.dp)),
    onCloseRequest: () -> Unit = ::exitApplication,
    visible: Boolean = true,
    title: String = "Compose Preview Lab",
    icon: Painter? = null,
    undecorated: Boolean = false,
    transparent: Boolean = false,
    resizable: Boolean = true,
    enabled: Boolean = true,
    focusable: Boolean = true,
    alwaysOnTop: Boolean = false,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
) {
    Window(
        onCloseRequest = onCloseRequest,
        state = windowState,
        visible = visible,
        title = title,
        icon = icon,
        undecorated = undecorated,
        transparent = transparent,
        resizable = resizable,
        enabled = enabled,
        focusable = focusable,
        alwaysOnTop = alwaysOnTop,
        onPreviewKeyEvent = onPreviewKeyEvent,
        onKeyEvent = onKeyEvent,
    ) {
        PreviewLabGallery(
            previewList = previewList,
            featuredFileList = featuredFileList,
            openFileHandler = openFileHandler,
            state = state,
            noSelectedContents = noSelectedContents,
        )
    }
}
