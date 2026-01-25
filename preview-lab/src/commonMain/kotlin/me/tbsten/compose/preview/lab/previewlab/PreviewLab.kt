package me.tbsten.compose.preview.lab.previewlab

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.floor
import kotlinx.serialization.json.Json
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.LocalIsInPreviewLabGalleryCardBody
import me.tbsten.compose.preview.lab.LocalPreviewLabPreview
import me.tbsten.compose.preview.lab.MutablePreviewLabField
import me.tbsten.compose.preview.lab.field.ScreenSize
import me.tbsten.compose.preview.lab.field.ScreenSizeField
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
import me.tbsten.compose.preview.lab.ui.util.thenIfNotNull
import me.tbsten.compose.preview.lab.ui.util.toDpOffset

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
 * ### Custom PreviewLab Instance
 * Create reusable PreviewLab configurations:
 *
 * ```kt
 * val myPreviewLab = PreviewLab(
 *   defaultScreenSizes = ScreenSize.AllPresets,
 *   defaultState = { remember { PreviewLabState() } }
 * )
 *
 * @Composable
 * private fun MyComponentPreview() = myPreviewLab {
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
 * ## Constructor Parameters
 *
 * @param defaultState
 *   Factory for creating the default PreviewLabState. Controls state persistence and initialization.
 *   By default, uses [rememberSaveable] for automatic state persistence across recompositions.
 *
 *   Usage examples:
 *   ```kt
 *   // Default behavior - state persists across recompositions
 *   PreviewLab() // Uses rememberSaveable internally
 *
 *   // Non-persistent state - resets on every recomposition
 *   PreviewLab(
 *     defaultState = { remember { PreviewLabState() } }
 *   )
 *
 *   // Custom retained state (requires Rin library)
 *   PreviewLab(
 *     defaultState = { remember { PreviewLabState() } }
 *   )
 *   ```
 *
 * @param defaultScreenSizes
 *   List of screen sizes to display in the preview. Controls which device form factors are available
 *   for testing. Defaults to [ScreenSize.SmartphoneAndDesktops].
 *
 *   Usage examples:
 *   ```kt
 *   // Default - smartphone and desktop sizes
 *   PreviewLab() // Uses ScreenSize.SmartphoneAndDesktops
 *
 *   // All available presets including tablets
 *   PreviewLab(
 *     defaultScreenSizes = ScreenSize.AllPresets
 *   )
 *
 *   // Only mobile devices for focused testing
 *   PreviewLab(
 *     defaultScreenSizes = listOf(ScreenSize.Phone, ScreenSize.Tablet)
 *   )
 *
 *   // Custom screen sizes for specific requirements
 *   PreviewLab(
 *     defaultScreenSizes = listOf(
 *       ScreenSize(360.dp, 640.dp, "Small Phone"),
 *       ScreenSize(1920.dp, 1080.dp, "Full HD Desktop")
 *     )
 *   )
 *   ```
 *
 * @param contentRoot
 *   Wrapper composable that surrounds the entire PreviewLab UI. Useful for providing custom
 *   themes, composition locals, or other global configuration.
 *
 *   Usage examples:
 *   ```kt
 *   // Default - no wrapper, uses PreviewLabTheme only
 *   PreviewLab() // Uses { it() }
 *
 *   // Apply custom Material3 theme like customizedPreviewLab
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
 *   )
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
 *   )
 *
 *   // Add global error boundary and logging
 *   PreviewLab(
 *     contentRoot = { content ->
 *       ErrorBoundary(onError = { error -> logError(error) }) {
 *         content()
 *       }
 *     }
 *   )
 *   ```
 *
 * @param defaultIsHeaderShow
 *   Controls whether the PreviewLab header is visible. When set to false, hides the header
 *   controls (scale, inspector panel toggle, etc.) to provide a cleaner preview view.
 *   Defaults to true.
 *
 *   Usage examples:
 *   ```kt
 *   // Default - header is visible
 *   PreviewLab() // Header shown
 *
 *   // Hide header for embedded preview
 *   PreviewLab(
 *     isHeaderShow = false
 *   ) {
 *     MyComponent()
 *   }
 *   ```
 *
 * @param defaultInspectorTabs
 *   List of tabs to display in the inspector panel.
 *   Defaults to [InspectorTab.defaults] which shows the built-in Fields and Events tabs.
 *
 *   Usage examples:
 *   ```kt
 *   // Default - Fields and Events tabs only
 *   PreviewLab() // Uses InspectorTab.defaults
 *
 *   // Add custom tabs alongside default tabs
 *   val myPreviewLab = PreviewLab(
 *     defaultInspectorTabs = { InspectorTab.defaults + listOf(DebugTab) }
 *   )
 *
 *   // Only custom tabs (no default Fields/Events)
 *   val customOnlyPreviewLab = PreviewLab(
 *     defaultInspectorTabs = { listOf(CustomTab1, CustomTab2) }
 *   )
 *
 *   // No tabs at all
 *   val noTabsPreviewLab = PreviewLab(
 *     defaultInspectorTabs = { emptyList() }
 *   )
 *   ```
 *
 * @param disableTrailingLambda
 *   Technical parameter to prevent the invoke method from being treated as a trailing lambda.
 *   Always null and has no functional purpose.
 *
 *   Usage example:
 *   ```kt
 *   // This parameter is always null - you don't need to specify it
 *   PreviewLab(
 *     defaultScreenSizes = ScreenSize.AllPresets
 *     // disableTrailingLambda is automatically null
 *   ) {
 *     // Preview content
 *   }
 *   ```
 *
 * @see PreviewLab.invoke Main entry point for creating preview content
 * @see PreviewLabState State management for preview controls and configuration
 * @see PreviewLabScope Scope providing field and event functions
 * @see ScreenSize Device screen size configuration for multi-device testing
 */
open class PreviewLab(
    private val defaultState: @Composable () -> PreviewLabState =
        { rememberPreviewLabStateFromUrl() },
    private val defaultScreenSizes: List<ScreenSize> = ScreenSize.SmartphoneAndDesktops,
    private val contentRoot: @Composable (content: @Composable () -> Unit) -> Unit = { it() },
    private val defaultIsHeaderShow: @Composable () -> Boolean = { LocalDefaultIsHeaderShow.current },
    private val defaultInspectorTabs: @Composable () -> List<InspectorTab> = { InspectorTab.defaults },
    @Suppress("unused") disableTrailingLambda: Nothing? = null,
) {
    /**
     * Convenience overload for single screen size preview.
     *
     * Use this when you want to test with a specific screen dimension instead of multiple sizes.
     * This is equivalent to calling the main invoke method with a single-item screenSizes list.
     *
     * ```kt
     * @Preview
     * @Composable
     * private fun TabletPreview() = PreviewLab(
     *   state = rememberPreviewLabState(),
     *   maxWidth = 1024.dp,
     *   maxHeight = 768.dp
     * ) {
     *   MyResponsiveComponent()
     * }
     * ```
     *
     * @param state PreviewLabState instance to use for this preview
     * @param maxWidth Maximum width constraint for the preview content
     * @param maxHeight Maximum height constraint for the preview content
     * @param modifier Modifier to apply to the PreviewLab container
     * @param isHeaderShow Controls whether the PreviewLab header is visible
     * @param content Preview content within PreviewLabScope
     * @see PreviewLab.invoke Main invoke method with multiple screen size support
     */
    @Composable
    operator fun invoke(
        maxWidth: Dp,
        maxHeight: Dp,
        modifier: Modifier = Modifier,
        state: PreviewLabState = defaultState(),
        isHeaderShow: Boolean = this.defaultIsHeaderShow(),
        inspectorTabs: List<InspectorTab> = this.defaultInspectorTabs(),
        isInPreviewLabGalleryCardBody: Boolean = LocalIsInPreviewLabGalleryCardBody.current,
        contentGraphicsLayer: GraphicsLayer = rememberGraphicsLayer(),
        content: @Composable PreviewLabScope.() -> Unit,
    ) = invoke(
        state = state,
        screenSizes = listOf(ScreenSize(maxWidth, maxHeight)),
        modifier = modifier,
        isHeaderShow = isHeaderShow,
        inspectorTabs = inspectorTabs,
        content = content,
    )

    /**
     * Main entry point for PreviewLab that enables interactive preview development with dynamic controls.
     *
     * This function wraps your preview content in a full-featured development environment that provides:
     * - Interactive field controls for dynamic parameter adjustment
     * - Event tracking and debugging capabilities
     * - Multi-device screen size testing
     * - Visual inspection tools with zoom and pan
     * - State persistence across recompositions
     *
     * ## Basic Example
     * ```kt
     * @Preview
     * @Composable
     * private fun MyButtonPreview() = PreviewLab { // this: PreviewLabScope
     *   val buttonText by fieldState { StringField("Text", "Click Me!") }
     *   val buttonColor by fieldState { EnumField<Color>("Color", Color.Blue) }
     *   val isEnabled by fieldState { BooleanField("Enabled", true) }
     *
     *   MyButton(
     *     text = buttonText,
     *     color = buttonColor,
     *     enabled = isEnabled,
     *     onClick = { onEvent("Button clicked", "User tapped the button") }
     *   )
     * }
     * ```
     *
     * ## Advanced Example with Multiple Screen Sizes
     * ```kt
     * @Preview
     * @Composable
     * private fun ResponsiveLayoutPreview() = PreviewLab(
     *   screenSizes = listOf(
     *     ScreenSize.Phone,
     *     ScreenSize.Tablet,
     *     ScreenSize.Desktop
     *   )
     * ) {
     *   val itemCount by fieldState { IntField("Item Count", 10) }
     *   val layoutStyle by fieldState { EnumField<LayoutStyle>("Layout", LayoutStyle.Grid) }
     *
     *   ResponsiveLayout(
     *     items = generateItems(itemCount),
     *     style = layoutStyle,
     *     onItemClick = { item -> onEvent("Item clicked", "Clicked item: ${item.id}") }
     *   )
     * }
     * ```
     *
     * ## Field Types Available
     * - **StringField**: Text input fields
     * - **BooleanField**: Toggle switches
     * - **IntField, FloatField, etc.**: Numeric input fields
     * - **EnumField**: Dropdown selection from enum values
     * - **SelectableField**: Custom dropdown with builder DSL
     * - **CombinedField**: Combine multiple fields into one
     * - Custom fields by extending PreviewLabField
     *
     * ## State Management
     * - Use **fieldState** for mutable fields that components can read and write
     * - Use **fieldValue** for read-only configuration parameters
     * - All field state is automatically persisted and restored
     *
     * ## Event Tracking
     * - **onEvent(title, description?)**: Log events with optional details
     * - Events show as toast notifications and are logged in the Events tab
     * - Useful for tracking user interactions and debugging component behavior
     *
     * @param state
     *   PreviewLabState instance that manages the preview's configuration and field values.
     *   Defaults to a saveable state that persists across recompositions. Override to provide
     *   custom state management or sharing state between multiple previews.
     *
     * @param screenSizes
     *   List of screen sizes to test the preview content against. The preview will show a
     *   screen size selector allowing switching between different device form factors.
     *   Must not be empty - use single-element list or the single-size overload for fixed sizing.
     *
     *   Common configurations:
     *   - `ScreenSize.SmartphoneAndDesktops` (default): Phone and desktop sizes
     *   - `ScreenSize.AllPresets`: All built-in device presets including tablets
     *   - `listOf(ScreenSize.Phone)`: Single phone size for focused testing
     *   - Custom list: `listOf(ScreenSize(360.dp, 640.dp), ScreenSize(1920.dp, 1080.dp))`
     *
     * @param modifier
     *   Modifier to apply to the PreviewLab container. Can be used to control size, background,
     *   padding, or other layout properties of the entire PreviewLab UI.
     *
     * @param isHeaderShow
     *   Controls whether the PreviewLab header is visible for this specific invocation.
     *   Defaults to the PreviewLab instance's configured value. When false, hides header controls
     *   to provide a cleaner preview view.
     *
     * @param inspectorTabs
     *   List of tabs to display in the inspector panel.
     *   Defaults to the PreviewLab instance's configured tabs, or [InspectorTab.defaults] if not configured.
     *
     *   Example usage:
     *   ```kt
     *   // Default tabs plus custom tab
     *   PreviewLab(inspectorTabs = InspectorTab.defaults + listOf(DebugTab)) {
     *       MyComponent()
     *   }
     *
     *   // Only custom tabs (no default Fields/Events)
     *   PreviewLab(inspectorTabs = listOf(CustomTab)) {
     *       MyComponent()
     *   }
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
    open operator fun invoke(
        modifier: Modifier = Modifier,
        state: PreviewLabState = defaultState(),
        screenSizes: List<ScreenSize> = defaultScreenSizes,
        isHeaderShow: Boolean = this.defaultIsHeaderShow(),
        inspectorTabs: List<InspectorTab> = this.defaultInspectorTabs(),
        isInPreviewLabGalleryCardBody: Boolean = LocalIsInPreviewLabGalleryCardBody.current,
        contentGraphicsLayer: GraphicsLayer = rememberGraphicsLayer(),
        content: @Composable PreviewLabScope.() -> Unit,
    ) {
        // Use LocalEnforcePreviewLabState if provided (e.g., from TestPreviewLab)
        @Suppress("NAME_SHADOWING")
        val state = LocalEnforcePreviewLabState.current ?: state

        if (isInPreviewLabGalleryCardBody) {
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

        Providers(state = state, toastHostState = toastHostState, captureScreenshot = captureScreenshot) {
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

    @OptIn(ExperimentalComposePreviewLabApi::class)
    @Composable
    private fun Providers(
        state: PreviewLabState,
        toastHostState: ToastHostState,
        captureScreenshot: suspend () -> androidx.compose.ui.graphics.ImageBitmap?,
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

    /**
     * PreviewLab companion object with default settings.
     *
     * This provides the most common way to use PreviewLab with sensible defaults:
     * - State persistence using rememberSaveable
     * - Smartphone and desktop screen sizes
     * - No custom content wrapper
     *
     * Most preview functions will use this default instance:
     * ```kt
     * @Preview
     * @Composable
     * private fun MyPreview() = PreviewLab {
     *   // Preview content with interactive fields
     * }
     * ```
     *
     * Equivalent to creating: `PreviewLab()` with default parameters.
     */
    companion object : PreviewLab()
}

@VisibleForTesting
val LocalEnforcePreviewLabState = compositionLocalOf<PreviewLabState?> { null }
internal val LocalToastHostState = compositionLocalOf<ToastHostState> { error("No ToastHostState") }

@Composable
private fun ContentSection(
    state: PreviewLabState,
    screenSizes: List<ScreenSize>,
    graphicsLayer: GraphicsLayer,
    modifier: Modifier = Modifier,
    content: @Composable PreviewLabScope.() -> Unit,
) {
    val density = LocalDensity.current
    val screenSize = state.scope
        .fieldValue {
            ScreenSizeField(
                sizes = screenSizes,
            )
        }

    Box(
        modifier = modifier
            .zIndex(-1f)
            .contentSectionBackground(state.contentOffset, state.contentScale, state.gridSize)
            .draggable2D(state.contentDraggableState)
            .graphicsLayer {
                translationX = state.contentOffset.x
                translationY = state.contentOffset.y
                scaleX = state.contentScale
                scaleY = state.contentScale
                transformOrigin = TransformOrigin(0f, 0f)
            }
            .fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .padding(32.dp)
                .layout { m, c ->
                    val p = m.measure(
                        c.copy(
                            maxWidth = screenSize.width.roundToPx(),
                            maxHeight = screenSize.height.roundToPx(),
                        ),
                    )
                    layout(p.width, p.height) {
                        val x =
                            if (c.maxWidth <= p.width) {
                                -((c.maxWidth - p.width) / 2)
                            } else {
                                0
                            }
                        val y =
                            if (c.maxHeight <= p.height) {
                                -((c.maxHeight - p.height) / 2)
                            } else {
                                0
                            }
                        p.place(x, y)
                    }
                },
        ) {
            Box(
                modifier = Modifier
                    .border(8.dp, PreviewLabTheme.colors.outline.copy(alpha = 0.25f))
                    .padding(8.dp)
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(graphicsLayer)
                    }
                    .onPlaced {
                        state.contentRootOffsetInAppRoot =
                            it.positionInRoot().toDpOffset(density)
                    },
            ) {
                content(state.scope)
            }
        }
    }
}

@Composable
private fun Modifier.contentSectionBackground(
    contentOffset: Offset,
    contentScale: Float,
    gridSize: Dp?,
    color: Color = PreviewLabTheme.colors.outlineSecondary,
) = thenIfNotNull(gridSize) { gridSize ->
    if (gridSize.value <= 0) {
        this
    } else {
        drawBehind {
            val gridSize = gridSize.toPx() * contentScale

            // grid
            val gridXStart = contentOffset.x - floor(contentOffset.x / gridSize) * gridSize
            var gridX = gridXStart
            while (gridX <= size.width) {
                drawLine(
                    color = color,
                    start = Offset(gridX, 0f),
                    end = Offset(gridX, size.height),
                    strokeWidth = 1.dp.toPx(),
                )

                gridX += gridSize
            }

            val gridYStart = contentOffset.y - floor(contentOffset.y / gridSize) * gridSize
            var gridY = gridYStart
            while (gridY <= size.height) {
                drawLine(
                    color = color,
                    start = Offset(0f, gridY),
                    end = Offset(size.width, gridY),
                    strokeWidth = 1.dp.toPx(),
                )

                gridY += gridSize
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

private val mcpBridgeJson = Json {
    ignoreUnknownKeys = true
    isLenient = false
}

/**
 * Effect that notifies the MCP bridge of PreviewLab state changes.
 */
@OptIn(ExperimentalComposePreviewLabApi::class)
@Composable
private fun McpBridgeEffect(
    mcpBridge: me.tbsten.compose.preview.lab.previewlab.mcp.PreviewLabMcpBridge,
    previewId: String,
    state: PreviewLabState,
    captureScreenshot: suspend () -> androidx.compose.ui.graphics.ImageBitmap?,
) {
    // Notify MCP bridge when fields or events change
    LaunchedEffect(mcpBridge, previewId, state) {
        snapshotFlow { state.fields.toList() to state.events.toList() }
            .collect { (fields, events) ->
                mcpBridge.updateState(
                    previewId = previewId,
                    fields = fields,
                    events = events,
                    captureScreenshot = captureScreenshot,
                    onUpdateField = { label, serializedValue ->
                        updateFieldValue(state, label, serializedValue)
                    },
                    onClearEvents = {
                        state.events.clear()
                    },
                )
            }
    }

    // Remove state when disposed
    DisposableEffect(mcpBridge, previewId) {
        onDispose {
            mcpBridge.removeState(previewId)
        }
    }
}

/**
 * Updates a field value from a serialized JSON string.
 */
@OptIn(ExperimentalComposePreviewLabApi::class)
@Suppress("UNCHECKED_CAST")
private fun updateFieldValue(state: PreviewLabState, label: String, serializedValue: String): Boolean {
    val field = state.fields.find { it.label == label } ?: return false
    if (field !is MutablePreviewLabField<*>) return false

    val serializer = field.serializer() ?: return false

    return try {
        val newValue = mcpBridgeJson.decodeFromString(serializer, serializedValue)
        (field as MutablePreviewLabField<Any?>).value = newValue
        true
    } catch (e: Exception) {
        println("[PreviewLab] Failed to update field '$label': ${e.message}")
        false
    }
}
