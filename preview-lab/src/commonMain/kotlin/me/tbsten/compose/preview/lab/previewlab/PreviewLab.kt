package me.tbsten.compose.preview.lab.previewlab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import me.tbsten.compose.preview.lab.field.ScreenSize
import me.tbsten.compose.preview.lab.previewlab.header.PreviewLabHeader
import me.tbsten.compose.preview.lab.previewlab.inspectorspane.InspectorTab
import me.tbsten.compose.preview.lab.previewlab.inspectorspane.InspectorsPane
import me.tbsten.compose.preview.lab.previewlab.screenshot.LocalCaptureScreenshot
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.toast.ToastAction
import me.tbsten.compose.preview.lab.ui.components.toast.ToastHost
import me.tbsten.compose.preview.lab.ui.components.toast.ToastHostState
import me.tbsten.compose.preview.lab.ui.components.toast.rememberToastHostState

/**
 * Interactive preview environment for Compose UI components. Wrap a `@Preview` body with
 * `PreviewLab { ... }` to gain dynamic field controls, event tracking, and multi-device
 * preview rendering.
 *
 * ```kt
 * @Preview
 * @Composable
 * private fun MyButtonPreview() = PreviewLab {
 *   val buttonText by fieldState { StringField("Text", "Click Me!") }
 *   val isEnabled by fieldState { BooleanField("Enabled", true) }
 *   val variant by fieldState { EnumField<ButtonVariant>("Variant", ButtonVariant.Primary) }
 *
 *   MyButton(text = buttonText, enabled = isEnabled, variant = variant,
 *     onClick = { onEvent("Button clicked") })
 * }
 * ```
 *
 * To centralise project-wide defaults (theme, screen sizes, locals), wrap `PreviewLab` in a
 * thin composable and call that from each `@Preview`:
 *
 * ```kt
 * @Composable
 * fun MyProjectPreviewLab(content: @Composable PreviewLabScope.() -> Unit) = PreviewLab(
 *   screenSizes = ScreenSize.AllPresets,
 *   contentRoot = { c -> MaterialTheme { c() } },
 *   content = content,
 * )
 * ```
 *
 * @param state State holder. Defaults to a [rememberSaveable]-backed instance via
 *   [PreviewLabDefaults.state].
 * @param screenSizes Device form factors to render simultaneously. Defaults to
 *   [PreviewLabDefaults.screenSizes] (smartphone + desktop). Pass [ScreenSize.AllPresets] or
 *   a custom list to override.
 * @param isHeaderShow When false, hides the top control bar — useful for embedded previews.
 * @param inspectorTabs Tabs in the right sidebar. Defaults to [InspectorTab.defaults] (Fields
 *   + Events). Pass `InspectorTab.defaults + listOf(DebugTab)` to add to the defaults.
 * @param contentRoot Wrapper composable surrounding the preview content (theme,
 *   `CompositionLocalProvider`, etc.). Defaults to a passthrough.
 * @param enable When false, renders only the content without the PreviewLab chrome. Defaults
 *   to `!LocalInspectionMode.current` — disabled in Android Studio's preview pane, enabled at
 *   runtime.
 * @param content Composable invoked with a [PreviewLabScope] receiver — see
 *   [PreviewLabScope] for `field` / `fieldState` / `fieldValue` / `onEvent`.
 * @param contentGraphicsLayer Backing GraphicsLayer for screenshot capture.
 *
 * @throws IllegalArgumentException if screenSizes is empty.
 * @see PreviewLabState
 * @see PreviewLabScope
 * @see ScreenSize
 */
@Composable
fun PreviewLab(
    modifier: Modifier = Modifier,
    state: PreviewLabState = PreviewLabDefaults.state(),
    screenSizes: List<ScreenSize> = PreviewLabDefaults.screenSizes(),
    showScreenSizeField: Boolean = true,
    isHeaderShow: Boolean = PreviewLabDefaults.isHeaderShow(),
    inspectorTabs: List<InspectorTab> = PreviewLabDefaults.inspectorTabs(),
    contentRoot: @Composable (content: @Composable () -> Unit) -> Unit = PreviewLabDefaults.contentRoot(),
    enable: Boolean = PreviewLabDefaults.enable(),
    isInPreviewLabGalleryCardBody: Boolean = PreviewLabDefaults.isInPreviewLabGalleryCardBody(),
    contentGraphicsLayer: GraphicsLayer = PreviewLabDefaults.contentGraphicsLayer(),
    content: @Composable PreviewLabScope.() -> Unit,
) {
    // Use LocalEnforcePreviewLabState if provided (e.g., from TestPreviewLab)
    @Suppress("NAME_SHADOWING", "VisibleForTests")
    val state = LocalEnforcePreviewLabState.current ?: state

    if (!enable || isInPreviewLabGalleryCardBody) {
        contentRoot {
            content(state.scope)
        }
        return
    }

    val toastHostState = rememberToastHostState().also { toastHostState ->
        state.scope.HandleEffect { event ->
            when (event) {
                is PreviewLabScope.Effect.ShowEventToast -> {
                    val toastId = toastHostState.show(
                        message = event.event.title,
                        action = ToastAction(
                            label = "Show Detail",
                            onClick = {
                                state.selectedTabIndex = InspectorTab.defaults.indexOf(InspectorTab.Events)
                                state.selectedEvent = event.event
                            },
                        ),
                    )
                }
            }
        }
    }

    val captureScreenshot: suspend () -> androidx.compose.ui.graphics.ImageBitmap? = remember(contentGraphicsLayer) {
        suspend { contentGraphicsLayer.toImageBitmap() }
    }

    PreviewLabProviders(
        state = state,
        toastHostState = toastHostState,
        captureScreenshot = captureScreenshot,
        contentRoot = contentRoot,
    ) {
        Box {
            Column(modifier = modifier.background(PreviewLabTheme.colors.background)) {
                PreviewLabHeader(
                    state = state,
                    isHeaderShow = isHeaderShow,
                    scale = state.contentScale,
                    onScaleChange = { state.contentScale = it },
                    onOffsetReset = { state.contentOffset = Offset.Zero },
                    onGridSizeChange = { state.gridSize = it },
                    isInspectorPanelVisible = state.isInspectorPanelVisible,
                    onIsInspectorPanelVisibleToggle = { state.isInspectorPanelVisible = !state.isInspectorPanelVisible },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    ) {
                        InspectorsPane(
                            state = state,
                            isVisible = state.isInspectorPanelVisible,
                            inspectorTabs = inspectorTabs,
                        ) {
                            ContentSection(
                                state = state,
                                screenSizes = screenSizes,
                                showScreenSizeField = showScreenSizeField,
                                graphicsLayer = contentGraphicsLayer,
                                content = content,
                                modifier = Modifier
                                    .weight(1f)
                                    .zIndex(-1f),
                            )
                        }
                    }
                }
            }

            ToastHost(
                state = toastHostState,
                maxVisibleToasts = 10,
            )
        }
    }
}

/**
 * Single-size convenience overload — equivalent to passing
 * `screenSizes = listOf(ScreenSize(maxWidth, maxHeight))`.
 *
 * @see PreviewLab
 */
@Composable
fun PreviewLab(
    maxWidth: Dp,
    maxHeight: Dp,
    modifier: Modifier = Modifier,
    state: PreviewLabState = PreviewLabDefaults.state(),
    showScreenSizeField: Boolean = true,
    isHeaderShow: Boolean = PreviewLabDefaults.isHeaderShow(),
    inspectorTabs: List<InspectorTab> = PreviewLabDefaults.inspectorTabs(),
    contentRoot: @Composable (content: @Composable () -> Unit) -> Unit = PreviewLabDefaults.contentRoot(),
    enable: Boolean = PreviewLabDefaults.enable(),
    isInPreviewLabGalleryCardBody: Boolean = PreviewLabDefaults.isInPreviewLabGalleryCardBody(),
    contentGraphicsLayer: GraphicsLayer = PreviewLabDefaults.contentGraphicsLayer(),
    content: @Composable PreviewLabScope.() -> Unit,
) = PreviewLab(
    modifier = modifier,
    state = state,
    screenSizes = listOf(ScreenSize(maxWidth, maxHeight)),
    isHeaderShow = isHeaderShow,
    inspectorTabs = inspectorTabs,
    contentRoot = contentRoot,
    enable = enable,
    isInPreviewLabGalleryCardBody = isInPreviewLabGalleryCardBody,
    contentGraphicsLayer = contentGraphicsLayer,
    content = content,
)

/**
 * Single-size convenience overload — equivalent to passing `screenSizes = listOf(screenSize)`.
 *
 * @see PreviewLab
 * @see ScreenSize
 */
@Composable
fun PreviewLab(
    screenSize: ScreenSize,
    modifier: Modifier = Modifier,
    state: PreviewLabState = PreviewLabDefaults.state(),
    showScreenSizeField: Boolean = true,
    isHeaderShow: Boolean = PreviewLabDefaults.isHeaderShow(),
    inspectorTabs: List<InspectorTab> = PreviewLabDefaults.inspectorTabs(),
    contentRoot: @Composable (content: @Composable () -> Unit) -> Unit = PreviewLabDefaults.contentRoot(),
    enable: Boolean = PreviewLabDefaults.enable(),
    isInPreviewLabGalleryCardBody: Boolean = PreviewLabDefaults.isInPreviewLabGalleryCardBody(),
    contentGraphicsLayer: GraphicsLayer = PreviewLabDefaults.contentGraphicsLayer(),
    content: @Composable PreviewLabScope.() -> Unit,
) = PreviewLab(
    modifier = modifier,
    state = state,
    screenSizes = listOf(screenSize),
    showScreenSizeField = showScreenSizeField,
    isHeaderShow = isHeaderShow,
    inspectorTabs = inspectorTabs,
    contentRoot = contentRoot,
    enable = enable,
    isInPreviewLabGalleryCardBody = isInPreviewLabGalleryCardBody,
    contentGraphicsLayer = contentGraphicsLayer,
    content = content,
)

@Composable
private fun PreviewLabProviders(
    state: PreviewLabState,
    toastHostState: ToastHostState,
    captureScreenshot: suspend () -> androidx.compose.ui.graphics.ImageBitmap?,
    contentRoot: @Composable (content: @Composable () -> Unit) -> Unit,
    content: @Composable () -> Unit,
) {
    val urlParams = rememberUrlParams()

    DisableSelection {
        contentRoot {
            PreviewLabTheme {
                CompositionLocalProvider(
                    @Suppress("VisibleForTests")
                    LocalEnforcePreviewLabState provides state,
                    LocalToastHostState provides toastHostState,
                    LocalCaptureScreenshot provides captureScreenshot,
                    LocalUrlParams provides urlParams,
                ) {
                    content()
                }
            }
        }
    }
}

val LocalDefaultIsHeaderShow = compositionLocalOf { true }

/**
 * Restores [PreviewLabState] (notably `contentScale`) from URL parameters on web targets.
 */
@Composable
fun rememberPreviewLabStateFromUrl(): PreviewLabState {
    val urlParams = rememberUrlParams()
    return rememberSaveable(saver = PreviewLabState.Saver) {
        PreviewLabState(
            initialContentScale = urlParams["contentScale"]?.toFloatOrNull() ?: 1f,
        )
    }
}
