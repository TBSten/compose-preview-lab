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
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.LocalPreviewLabPreview
import me.tbsten.compose.preview.lab.field.ScreenSize
import me.tbsten.compose.preview.lab.previewlab.header.PreviewLabHeader
import me.tbsten.compose.preview.lab.previewlab.inspectorspane.InspectorTab
import me.tbsten.compose.preview.lab.previewlab.inspectorspane.InspectorsPane
import me.tbsten.compose.preview.lab.previewlab.mcp.LocalPreviewLabMcpBridge
import me.tbsten.compose.preview.lab.previewlab.screenshot.LocalCaptureScreenshot
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.toast.ToastAction
import me.tbsten.compose.preview.lab.ui.components.toast.ToastHost
import me.tbsten.compose.preview.lab.ui.components.toast.ToastHostState
import me.tbsten.compose.preview.lab.ui.components.toast.rememberToastHostState

/**
 * PreviewLab is a powerful preview environment for Compose UI components that enables interactive development and testing.
 * It provides dynamic field controls, event tracking, and multi-device previews to enhance the development experience.
 *
 * ## Basic Usage
 *
 * Use PreviewLab as a wrapper around your preview composables to enable interactive controls:
 *
 * ```kt
 * @Preview
 * @Composable
 * private fun MyButtonPreview() = PreviewLab {
 *   val buttonText by fieldState { StringField("Text", "Click Me!") }
 *   val isEnabled by fieldState { BooleanField("Enabled", true) }
 *   val variant by fieldState { EnumField<ButtonVariant>("Variant", ButtonVariant.Primary) }
 *
 *   MyButton(
 *     text = buttonText,
 *     enabled = isEnabled,
 *     variant = variant,
 *     onClick = { onEvent("Button clicked") }
 *   )
 * }
 * ```
 *
 * ## Key Features
 *
 * ### Interactive Fields
 * - **fieldState**: Creates mutable fields for interactive state management
 * - **fieldValue**: Creates read-only fields for parameter configuration
 * - Built-in field types: StringField, BooleanField, IntField, EnumField, SelectableField, etc.
 *
 * ### Event Tracking
 * - **onEvent**: Records user interactions and displays toast notifications
 * - Events are logged in the Events tab for debugging and testing
 *
 * ### Multi-Device Preview
 * - **screenSizes**: Test components across different screen sizes simultaneously
 * - Built-in presets for smartphones, tablets, and desktop devices
 *
 * ### Visual Controls
 * - **Inspector Panel**: Right sidebar with fields, events, and settings
 * - **Zoom and Pan**: Scale and move preview content for detailed inspection
 * - **Device Frames**: Visual device boundaries for accurate sizing
 *
 * ## Advanced Usage
 *
 * ### Custom PreviewLab Wrapper
 * Create reusable PreviewLab configurations by wrapping PreviewLab in your own Composable function:
 *
 * ```kt
 * @Composable
 * fun MyProjectPreviewLab(
 *   modifier: Modifier = Modifier,
 *   content: @Composable PreviewLabScope.() -> Unit,
 * ) = PreviewLab(
 *   modifier = modifier,
 *   state = remember { PreviewLabState() },
 *   screenSizes = ScreenSize.AllPresets,
 *   contentRoot = { c -> MaterialTheme { c() } },
 *   content = content,
 * )
 *
 * @Composable
 * private fun MyComponentPreview() = MyProjectPreviewLab {
 *   MyComponent()
 * }
 * ```
 *
 * ### Complex Field Combinations
 * Combine multiple fields for comprehensive testing:
 *
 * ```kt
 * PreviewLab {
 *   val theme by fieldState { EnumField<AppTheme>("Theme", AppTheme.Light) }
 *   val language by fieldState { SelectableField<Locale>("Language") {
 *     choice(Locale.ENGLISH, "English", isDefault = true)
 *     choice(Locale.JAPANESE, "日本語")
 *   }}
 *   val isLoading by fieldState { BooleanField("Loading State", false) }
 *
 *   MyApp(theme = theme, locale = language, isLoading = isLoading)
 * }
 * ```
 *
 * ## Parameters
 *
 * @param modifier
 *   Modifier to apply to the PreviewLab container. Can be used to control size, background,
 *   padding, or other layout properties of the entire PreviewLab UI.
 *
 * @param state
 *   PreviewLabState instance that manages the preview's configuration and field values.
 *   Defaults to a saveable state that persists across recompositions. Override to provide
 *   custom state management or sharing state between multiple previews.
 *
 *   Usage examples:
 *   ```kt
 *   // Default behavior - state persists across recompositions
 *   PreviewLab { ... } // Uses rememberSaveable internally
 *
 *   // Non-persistent state - resets on every recomposition
 *   PreviewLab(
 *     state = remember { PreviewLabState() }
 *   ) { ... }
 *   ```
 *
 * @param screenSizes
 *   List of screen sizes to display in the preview. Controls which device form factors are available
 *   for testing. Defaults to [ScreenSize.SmartphoneAndDesktops].
 *
 *   Usage examples:
 *   ```kt
 *   // Default - smartphone and desktop sizes
 *   PreviewLab { ... } // Uses ScreenSize.SmartphoneAndDesktops
 *
 *   // All available presets including tablets
 *   PreviewLab(
 *     screenSizes = ScreenSize.AllPresets
 *   ) { ... }
 *
 *   // Only mobile devices for focused testing
 *   PreviewLab(
 *     screenSizes = listOf(ScreenSize.Phone, ScreenSize.Tablet)
 *   ) { ... }
 *
 *   // Custom screen sizes for specific requirements
 *   PreviewLab(
 *     screenSizes = listOf(
 *       ScreenSize(360.dp, 640.dp, "Small Phone"),
 *       ScreenSize(1920.dp, 1080.dp, "Full HD Desktop")
 *     )
 *   ) { ... }
 *   ```
 *
 * @param isHeaderShow
 *   Controls whether the PreviewLab header is visible. When set to false, hides the header
 *   controls (scale, inspector panel toggle, etc.) to provide a cleaner preview view.
 *   Defaults to true.
 *
 *   Usage examples:
 *   ```kt
 *   // Default - header is visible
 *   PreviewLab { ... } // Header shown
 *
 *   // Hide header for embedded preview
 *   PreviewLab(
 *     isHeaderShow = false
 *   ) {
 *     MyComponent()
 *   }
 *   ```
 *
 * @param inspectorTabs
 *   List of tabs to display in the inspector panel.
 *   Defaults to [InspectorTab.defaults] which shows the built-in Fields and Events tabs.
 *
 *   Usage examples:
 *   ```kt
 *   // Default - Fields and Events tabs only
 *   PreviewLab { ... } // Uses InspectorTab.defaults
 *
 *   // Add custom tabs alongside default tabs
 *   PreviewLab(
 *     inspectorTabs = InspectorTab.defaults + listOf(DebugTab)
 *   ) { ... }
 *
 *   // Only custom tabs (no default Fields/Events)
 *   PreviewLab(
 *     inspectorTabs = listOf(CustomTab1, CustomTab2)
 *   ) { ... }
 *
 *   // No tabs at all
 *   PreviewLab(
 *     inspectorTabs = emptyList()
 *   ) { ... }
 *   ```
 *
 * @param contentRoot
 *   Wrapper composable that surrounds the entire PreviewLab UI. Useful for providing custom
 *   themes, composition locals, or other global configuration.
 *
 *   Usage examples:
 *   ```kt
 *   // Default - no wrapper, uses PreviewLabTheme only
 *   PreviewLab { ... } // Uses { it() }
 *
 *   // Apply custom Material3 theme
 *   PreviewLab(
 *     contentRoot = { content ->
 *       MaterialTheme(
 *         colorScheme = lightColorScheme(
 *           primary = Color.Red,
 *           onPrimary = Color.Yellow
 *         )
 *       ) {
 *         content()
 *       }
 *     }
 *   ) { ... }
 *
 *   // Provide composition locals for your design system
 *   PreviewLab(
 *     contentRoot = { content ->
 *       MyDesignSystem {
 *         CompositionLocalProvider(
 *           LocalCustomTypography provides customTypography,
 *           LocalBranding provides myBranding
 *         ) {
 *           content()
 *         }
 *       }
 *     }
 *   ) { ... }
 *   ```
 *
 * @param enable
 *   Controls whether PreviewLab UI is enabled. When set to false, only the content is rendered
 *   without the PreviewLab wrapper (header, inspector panel, screen size controls, etc.).
 *   Defaults to `!LocalInspectionMode.current`, which means PreviewLab UI is disabled during
 *   Android Studio preview and enabled during normal runtime.
 *
 *   Usage examples:
 *   ```kt
 *   // Default - disabled in Android Studio preview, enabled at runtime
 *   PreviewLab { ... }
 *
 *   // Always enable PreviewLab UI
 *   PreviewLab(
 *     enable = true
 *   ) { ... }
 *
 *   // Always disable PreviewLab UI (only content is shown)
 *   PreviewLab(
 *     enable = false
 *   ) { ... }
 *   ```
 *
 * @param content
 *   Preview content lambda with PreviewLabScope receiver. Within this scope you have access to:
 *   - **fieldState { ... }**: Create mutable state fields
 *   - **fieldValue { ... }**: Create read-only parameter fields
 *   - **onEvent(title, description?)**: Log events for tracking and debugging
 *
 *   The content will be rendered within the selected screen size constraints and can use
 *   layout modifiers like fillMaxSize() which will be bounded by the selected screen size.
 *
 * @param contentGraphicsLayer GraphicsLayer for capturing screenshots. Defaults to rememberGraphicsLayer().
 *
 * @throws IllegalArgumentException if screenSizes is empty
 * @see PreviewLabState State management and persistence
 * @see PreviewLabScope Scope providing field and event functions
 * @see ScreenSize Device screen size definitions and presets
 */
@Composable
fun PreviewLab(
    modifier: Modifier = Modifier,
    state: PreviewLabState = PreviewLabDefaults.state(),
    screenSizes: List<ScreenSize> = PreviewLabDefaults.screenSizes(),
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
 * Convenience overload for single screen size preview.
 *
 * Use this when you want to test with a specific screen dimension instead of multiple sizes.
 * This is equivalent to calling the main PreviewLab function with a single-item screenSizes list.
 *
 * ```kt
 * @Preview
 * @Composable
 * private fun TabletPreview() = PreviewLab(
 *   maxWidth = 1024.dp,
 *   maxHeight = 768.dp
 * ) {
 *   MyResponsiveComponent()
 * }
 * ```
 *
 * @param maxWidth Maximum width constraint for the preview content
 * @param maxHeight Maximum height constraint for the preview content
 * @param modifier Modifier to apply to the PreviewLab container
 * @param state PreviewLabState instance to use for this preview
 * @param isHeaderShow Controls whether the PreviewLab header is visible
 * @param inspectorTabs List of tabs to display in the inspector panel
 * @param contentRoot Wrapper composable for custom themes or composition locals
 * @param enable Controls whether PreviewLab UI is enabled. Defaults to `!LocalInspectionMode.current`
 * @param content Preview content within PreviewLabScope
 * @see PreviewLab Main PreviewLab function with multiple screen size support
 */
@Composable
fun PreviewLab(
    maxWidth: Dp,
    maxHeight: Dp,
    modifier: Modifier = Modifier,
    state: PreviewLabState = PreviewLabDefaults.state(),
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
 * Convenience overload for single screen size preview using a [ScreenSize] object.
 *
 * Use this when you want to test with a specific screen size preset instead of multiple sizes.
 * This is equivalent to calling the main PreviewLab function with a single-item screenSizes list.
 *
 * ```kt
 * @Preview
 * @Composable
 * private fun PhonePreview() = PreviewLab(
 *   screenSize = ScreenSize.Phone
 * ) {
 *   MyResponsiveComponent()
 * }
 * ```
 *
 * @param screenSize The screen size to use for the preview
 * @param modifier Modifier to apply to the PreviewLab container
 * @param state PreviewLabState instance to use for this preview
 * @param isHeaderShow Controls whether the PreviewLab header is visible
 * @param inspectorTabs List of tabs to display in the inspector panel
 * @param contentRoot Wrapper composable for custom themes or composition locals
 * @param enable Controls whether PreviewLab UI is enabled. Defaults to `!LocalInspectionMode.current`
 * @param content Preview content within PreviewLabScope
 * @see PreviewLab Main PreviewLab function with multiple screen size support
 * @see ScreenSize Device screen size definitions and presets
 */
@Composable
fun PreviewLab(
    screenSize: ScreenSize,
    modifier: Modifier = Modifier,
    state: PreviewLabState = PreviewLabDefaults.state(),
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
    isHeaderShow = isHeaderShow,
    inspectorTabs = inspectorTabs,
    contentRoot = contentRoot,
    enable = enable,
    isInPreviewLabGalleryCardBody = isInPreviewLabGalleryCardBody,
    contentGraphicsLayer = contentGraphicsLayer,
    content = content,
)

@OptIn(ExperimentalComposePreviewLabApi::class)
@Composable
private fun PreviewLabProviders(
    state: PreviewLabState,
    toastHostState: ToastHostState,
    captureScreenshot: suspend () -> androidx.compose.ui.graphics.ImageBitmap?,
    contentRoot: @Composable (content: @Composable () -> Unit) -> Unit,
    content: @Composable () -> Unit,
) {
    val urlParams = rememberUrlParams()
    val mcpBridge = LocalPreviewLabMcpBridge.current
    val previewId = LocalPreviewLabPreview.current?.id

    // Notify MCP bridge of state changes (only if previewId is available)
    if (previewId != null) {
        McpBridgeEffect(
            mcpBridge = mcpBridge,
            previewId = previewId,
            state = state,
            captureScreenshot = captureScreenshot,
        )
    }

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
 * URLパラメータからPreviewLabStateを初期化するComposable関数。
 * contentScaleなどの状態をURLパラメータから復元する。
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
