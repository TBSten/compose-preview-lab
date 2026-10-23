package me.tbsten.compose.preview.lab.extension.debugger.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import me.tbsten.compose.preview.lab.ui.Colors
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme

/**
 * Theme configuration for DebugMenu components.
 *
 * Uses [PreviewLabTheme] for consistent styling with the rest of the library.
 */
object DebugMenuTheme {
    /**
     * The color scheme used by DebugMenu components.
     */
    val colors: Colors
        @Composable
        @ReadOnlyComposable
        get() = PreviewLabTheme.colors

    /**
     * Background color for the DebugMenu.
     */
    val backgroundColor: Color
        @Composable
        @ReadOnlyComposable
        get() = colors.background

    /**
     * Content color (text, icons) for the DebugMenu.
     */
    val contentColor: Color
        @Composable
        @ReadOnlyComposable
        get() = colors.onBackground
}

/**
 * Wrapper for DebugMenu content.
 *
 * Currently, a pass-through that uses the existing [PreviewLabTheme].
 * Can be extended in the future to provide custom theming.
 *
 * @param content The content to wrap
 */
@Composable
internal fun DebugMenuThemeProvider(content: @Composable () -> Unit) = PreviewLabTheme {
    content()
}
