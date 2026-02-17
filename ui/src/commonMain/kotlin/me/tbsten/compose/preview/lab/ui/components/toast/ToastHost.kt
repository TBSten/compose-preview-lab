package me.tbsten.compose.preview.lab.ui.components.toast

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi

@Composable
@UiComposePreviewLabApi
fun ToastHost(state: ToastHostState, modifier: Modifier = Modifier, maxVisibleToasts: Int = 5) {
    val visibleToasts = state.toasts.takeLast(maxVisibleToasts)
    val toastCount = visibleToasts.size

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            visibleToasts.forEachIndexed { index, toast ->
                key(toast.id) {
                    // index 0 = oldest (back), index last = newest (front)
                    val stackIndex = toastCount - 1 - index
                    val targetScale = 1f - (stackIndex * 0.05f)
                    val targetOffsetY = stackIndex * -8f

                    val animatedScale = animateFloatAsState(
                        targetValue = targetScale,
                        animationSpec = tween(durationMillis = 200),
                        label = "toast_scale",
                    )
                    val animatedOffsetY = animateFloatAsState(
                        targetValue = targetOffsetY,
                        animationSpec = tween(durationMillis = 200),
                        label = "toast_offsetY",
                    )

                    ToastItem(
                        toast = toast,
                        onDismiss = { state.removeToast(toast) },
                        modifier = Modifier
                            .zIndex(index.toFloat())
                            .graphicsLayer {
                                scaleX = animatedScale.value
                                scaleY = animatedScale.value
                                translationY = animatedOffsetY.value.dp.toPx()
                            },
                    )
                }
            }
        }
    }
}
