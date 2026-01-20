package me.tbsten.compose.preview.lab.ui.components.toast

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme

@InternalComposePreviewLabApi
public object ToastDefaults {
    public val shape: Shape = RoundedCornerShape(8.dp)
    public val shadowElevation: Dp = 4.dp

    /** Duration in milliseconds for toast enter/exit animations */
    public const val AnimationDurationMillis: Long = 300L

    @Composable
    public fun containerColor(type: ToastType): Color = when (type) {
        ToastType.Default -> PreviewLabTheme.colors.surface
        ToastType.Success -> PreviewLabTheme.colors.success
        ToastType.Error -> PreviewLabTheme.colors.error
        ToastType.Info -> PreviewLabTheme.colors.tertiary
    }

    @Composable
    public fun contentColor(type: ToastType): Color = when (type) {
        ToastType.Default -> PreviewLabTheme.colors.onSurface
        ToastType.Success -> PreviewLabTheme.colors.onSuccess
        ToastType.Error -> PreviewLabTheme.colors.onError
        ToastType.Info -> PreviewLabTheme.colors.onTertiary
    }
}
