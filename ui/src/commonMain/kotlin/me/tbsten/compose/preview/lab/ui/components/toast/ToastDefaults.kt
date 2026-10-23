package me.tbsten.compose.preview.lab.ui.components.toast

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme

/**
 * Styling defaults for [ToastItem]: a floating panel in the Composables UI idiom (12dp radius, soft shadow,
 * 1dp outline on the neutral toast) painted with the [PreviewLabTheme] facade colours.
 */
internal object ToastDefaults {
    val shape: Shape = RoundedCornerShape(12.dp)
    val shadowElevation: Dp = 6.dp
    val borderWidth: Dp = 1.dp

    /** Minimum height of a toast (text-only toasts; toasts with buttons grow to fit them). */
    val MinHeight: Dp = 44.dp

    /** Duration in milliseconds for toast enter/exit animations */
    const val AnimationDurationMillis: Long = 300L

    @Composable
    @ReadOnlyComposable
    fun containerColor(type: ToastType): Color = when (type) {
        ToastType.Default -> PreviewLabTheme.colors.panel
        ToastType.Success -> PreviewLabTheme.colors.success
        ToastType.Error -> PreviewLabTheme.colors.error
        ToastType.Info -> PreviewLabTheme.colors.tertiary
    }

    @Composable
    @ReadOnlyComposable
    fun contentColor(type: ToastType): Color = when (type) {
        ToastType.Default -> PreviewLabTheme.colors.onPanel
        ToastType.Success -> PreviewLabTheme.colors.onSuccess
        ToastType.Error -> PreviewLabTheme.colors.onError
        ToastType.Info -> PreviewLabTheme.colors.onTertiary
    }

    /** 1dp outline separating the panel-coloured [ToastType.Default] toast from panel-coloured chrome; none otherwise. */
    @Composable
    @ReadOnlyComposable
    fun border(type: ToastType): BorderStroke? = when (type) {
        ToastType.Default -> BorderStroke(borderWidth, PreviewLabTheme.colors.outline)
        else -> null
    }
}
