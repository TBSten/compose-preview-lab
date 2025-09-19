package me.tbsten.compose.preview.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.dokar.sonner.TextToastAction
import com.dokar.sonner.Toaster
import com.dokar.sonner.ToasterState
import com.dokar.sonner.rememberToasterState
import me.tbsten.compose.preview.lab.component.header.PreviewLabHeader
import me.tbsten.compose.preview.lab.component.inspectorspane.InspectorTab
import me.tbsten.compose.preview.lab.component.inspectorspane.InspectorsPane
import me.tbsten.compose.preview.lab.field.ScreenSize
import me.tbsten.compose.preview.lab.field.ScreenSizeField
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.util.toDpOffset

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
 *   defaultState = { rememberRetained { PreviewLabState() } }
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
 *     defaultState = { rememberRetained { PreviewLabState() } }
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
        { rememberSaveable(saver = PreviewLabState.Saver) { PreviewLabState() } },
    private val defaultScreenSizes: List<ScreenSize> = ScreenSize.SmartphoneAndDesktops,
    private val contentRoot: @Composable (content: @Composable () -> Unit) -> Unit = { it() },
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
     * @param content Preview content within PreviewLabScope
     * @see PreviewLab.invoke Main invoke method with multiple screen size support
     */
    @Composable
    operator fun invoke(
        state: PreviewLabState = defaultState(),
        maxWidth: Dp,
        maxHeight: Dp,
        content: @Composable PreviewLabScope.() -> Unit,
    ) = invoke(
        state = state,
        screenSizes = listOf(ScreenSize(maxWidth, maxHeight)),
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
     * @param content
     *   Preview content lambda with PreviewLabScope receiver. Within this scope you have access to:
     *   - **fieldState { ... }**: Create mutable state fields
     *   - **fieldValue { ... }**: Create read-only parameter fields
     *   - **onEvent(title, description?)**: Log events for tracking and debugging
     *
     *   The content will be rendered within the selected screen size constraints and can use
     *   layout modifiers like fillMaxSize() which will be bounded by the selected screen size.
     *
     * @throws IllegalArgumentException if screenSizes is empty
     * @see PreviewLabState State management and persistence
     * @see PreviewLabScope Scope providing field and event functions
     * @see ScreenSize Device screen size definitions and presets
     */
    @Composable
    open operator fun invoke(
        state: PreviewLabState = defaultState(),
        screenSizes: List<ScreenSize> = defaultScreenSizes,
        content: @Composable PreviewLabScope.() -> Unit,
    ) {
        val toaster = rememberToasterState().also { toaster ->
            state.scope.HandleEffect { event ->
                when (event) {
                    is PreviewLabScope.Effect.ShowEventToast ->
                        toaster.show(
                            message = event.event.title,
                            action = TextToastAction(
                                text = "Show Detail",
                                onClick = {
                                    state.selectedTabIndex = InspectorTab.entries.indexOf(InspectorTab.Events)
                                    state.selectedEvent = event.event
                                    toaster.dismiss(it)
                                },
                            ),
                        )
                }
            }
        }

        Providers(state = state, toaster = toaster) {
            Column(modifier = Modifier.background(PreviewLabTheme.colors.background)) {
                PreviewLabHeader(
                    scale = state.contentScale,
                    onScaleChange = { state.contentScale = it },
                    isInspectorPanelVisible = state.isInspectorPanelVisible,
                    onIsInspectorPanelVisibleToggle = { state.isInspectorPanelVisible = !state.isInspectorPanelVisible },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    ) {
                        InspectorsPane(state = state, isVisible = state.isInspectorPanelVisible) {
                            ContentSection(
                                state = state,
                                screenSizes = screenSizes,
                                content = content,
                                modifier = Modifier
                                    .weight(1f)
                                    .zIndex(-1f),
                            )
                        }
                    }
                }
            }

            Toaster(
                state = toaster,
                maxVisibleToasts = 10,
                showCloseButton = true,
            )
        }
    }

    @Composable
    private fun Providers(state: PreviewLabState, toaster: ToasterState, content: @Composable () -> Unit) {
        contentRoot {
            PreviewLabTheme {
                CompositionLocalProvider(
                    LocalPreviewLabState provides state,
                    LocalToaster provides toaster,
                ) {
                    content()
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

internal val LocalPreviewLabState = compositionLocalOf<PreviewLabState?> { null }
internal val LocalToaster = compositionLocalOf<ToasterState> { error("No ToasterState") }

@Composable
private fun ContentSection(
    state: PreviewLabState,
    screenSizes: List<ScreenSize>,
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
            .draggable2D(state.contentDraggableState)
            .graphicsLayer {
                translationX = state.contentOffset.x
                translationY = state.contentOffset.y
                scaleX = state.contentScale
                scaleY = state.contentScale
                transformOrigin = TransformOrigin(0f, 0f)
            },
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
