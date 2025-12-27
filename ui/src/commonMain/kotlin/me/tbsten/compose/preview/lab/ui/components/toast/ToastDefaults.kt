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
object ToastDefaults {
    val shape: Shape = RoundedCornerShape(8.dp)
    val shadowElevation: Dp = 4.dp

    @Composable
    fun containerColor(type: ToastType): Color = when (type) {
        ToastType.Default -> PreviewLabTheme.colors.surface
        ToastType.Success -> PreviewLabTheme.colors.success
        ToastType.Error -> PreviewLabTheme.colors.error
        ToastType.Info -> PreviewLabTheme.colors.tertiary
    }

    @Composable
    fun contentColor(type: ToastType): Color = when (type) {
        ToastType.Default -> PreviewLabTheme.colors.onSurface
        ToastType.Success -> PreviewLabTheme.colors.onSuccess
        ToastType.Error -> PreviewLabTheme.colors.onError
        ToastType.Info -> PreviewLabTheme.colors.onTertiary
    }
}
