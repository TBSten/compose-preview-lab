package me.tbsten.compose.preview.lab.ui.components.toast

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi

@Composable
@InternalComposePreviewLabApi
fun ToastHost(state: ToastHostState, modifier: Modifier = Modifier, maxVisibleToasts: Int = 10,) {
    val visibleToasts = state.toasts.takeLast(maxVisibleToasts)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            visibleToasts.forEach { toast ->
                ToastItem(
                    toast = toast,
                    onDismiss = { state.removeToast(toast) },
                )
            }
        }
    }
}
