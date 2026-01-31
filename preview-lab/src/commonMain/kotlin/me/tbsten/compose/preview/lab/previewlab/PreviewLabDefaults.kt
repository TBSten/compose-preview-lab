package me.tbsten.compose.preview.lab.previewlab

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import me.tbsten.compose.preview.lab.LocalIsInPreviewLabGalleryCardBody
import me.tbsten.compose.preview.lab.field.ScreenSize
import me.tbsten.compose.preview.lab.previewlab.inspectorspane.InspectorTab

/**
 * Contains the default values used by [PreviewLab].
 *
 * This object provides sensible defaults for PreviewLab parameters that can be used
 * when creating custom PreviewLab wrappers or when you want to reference the default values.
 *
 * ## Usage
 *
 * ```kt
 * @Composable
 * fun MyCustomPreviewLab(
 *     modifier: Modifier = Modifier,
 *     screenSizes: List<ScreenSize> = PreviewLabDefaults.screenSizes,
 *     isHeaderShow: Boolean = PreviewLabDefaults.isHeaderShow,
 *     content: @Composable PreviewLabScope.() -> Unit,
 * ) = PreviewLab(
 *     modifier = modifier,
 *     screenSizes = screenSizes,
 *     isHeaderShow = isHeaderShow,
 *     content = content,
 * )
 * ```
 *
 * @see PreviewLab
 */
object PreviewLabDefaults {

    /**
     * Default screen sizes for PreviewLab.
     *
     * Uses [ScreenSize.SmartphoneAndDesktops] which includes common smartphone
     * and desktop screen sizes for responsive testing.
     */
    val screenSizes: List<ScreenSize> = ScreenSize.SmartphoneAndDesktops

    /**
     * Default visibility for the PreviewLab header.
     *
     * When `true`, the header with scale controls, inspector panel toggle,
     * and other controls is visible.
     *
     * This value respects [LocalDefaultIsHeaderShow] CompositionLocal,
     * allowing embedded contexts to override the default.
     */
    val isHeaderShow: Boolean
        @Composable get() = LocalDefaultIsHeaderShow.current

    /**
     * Default inspector tabs for PreviewLab.
     *
     * Uses [InspectorTab.defaults] which includes the built-in Fields and Events tabs.
     */
    val inspectorTabs: List<InspectorTab> = InspectorTab.defaults

    /**
     * Default content root wrapper.
     *
     * Simply invokes the content without any additional wrapping.
     * Override this to provide custom themes or composition locals.
     */
    val contentRoot: @Composable (content: @Composable () -> Unit) -> Unit = { it() }

    /**
     * Default enable state for PreviewLab UI.
     *
     * Returns `true` when not in inspection mode (normal runtime),
     * and `false` during Android Studio preview.
     *
     * This allows previews to render quickly in Android Studio while
     * providing full PreviewLab functionality at runtime.
     */
    val enable: Boolean
        @Composable get() = !LocalInspectionMode.current

    /**
     * Default state for PreviewLab.
     *
     * Uses [rememberPreviewLabStateFromUrl] which initializes state from URL parameters
     * and persists across recompositions using rememberSaveable.
     */
    val state: PreviewLabState
        @Composable get() = rememberPreviewLabStateFromUrl()

    /**
     * Default value indicating whether PreviewLab is inside a Gallery card body.
     *
     * When `true`, PreviewLab renders only the content without the full UI wrapper.
     * This value is read from [LocalIsInPreviewLabGalleryCardBody] CompositionLocal.
     */
    val isInPreviewLabGalleryCardBody: Boolean
        @Composable get() = LocalIsInPreviewLabGalleryCardBody.current

    /**
     * Default graphics layer for capturing screenshots.
     *
     * Uses [rememberGraphicsLayer] to create a new GraphicsLayer instance.
     */
    val contentGraphicsLayer: GraphicsLayer
        @Composable get() = rememberGraphicsLayer()
}
