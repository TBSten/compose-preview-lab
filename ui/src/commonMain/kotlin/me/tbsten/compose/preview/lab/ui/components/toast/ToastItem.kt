package me.tbsten.compose.preview.lab.ui.components.toast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.components.Icon
import me.tbsten.compose.preview.lab.ui.components.Surface
import me.tbsten.compose.preview.lab.ui.components.Text
import me.tbsten.compose.preview.lab.ui.generated.resources.PreviewLabUiRes
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_close
import org.jetbrains.compose.resources.painterResource

@Composable
@InternalComposePreviewLabApi
fun ToastItem(toast: ToastData, onDismiss: () -> Unit, modifier: Modifier = Modifier,) {
    var isVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(toast.id) {
        isVisible = true
        if (toast.duration != ToastDuration.Indefinite) {
            delay(toast.duration.millis)
            isVisible = false
            delay(ToastDefaults.AnimationDurationMillis)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(initialScale = 0.9f) + fadeIn(),
        exit = scaleOut(targetScale = 0.9f) + fadeOut(),
        modifier = modifier.testTag("toast_${toast.id}"),
    ) {
        val containerColor = ToastDefaults.containerColor(toast.type)
        val contentColor = ToastDefaults.contentColor(toast.type)

        Surface(
            shape = ToastDefaults.shape,
            color = containerColor,
            contentColor = contentColor,
            shadowElevation = ToastDefaults.shadowElevation,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = toast.message,
                    color = contentColor,
                    modifier = Modifier.weight(1f, fill = false),
                )

                toast.action?.let { action ->
                    Text(
                        text = action.label,
                        color = contentColor,
                        modifier = Modifier
                            .clickable(role = Role.Button) { action.onClick() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }

                if (toast.showCloseButton) {
                    Icon(
                        painter = painterResource(PreviewLabUiRes.drawable.icon_close),
                        contentDescription = "Close",
                        tint = contentColor,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(role = Role.Button) {
                                coroutineScope.launch {
                                    isVisible = false
                                    delay(ToastDefaults.AnimationDurationMillis)
                                    onDismiss()
                                }
                            },
                    )
                }
            }
        }
    }
}
