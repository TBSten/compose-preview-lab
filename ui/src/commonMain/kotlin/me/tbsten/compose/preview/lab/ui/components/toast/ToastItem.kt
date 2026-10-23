package me.tbsten.compose.preview.lab.ui.components.toast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.tbsten.compose.preview.lab.ui.components.PreviewLabButton
import me.tbsten.compose.preview.lab.ui.components.PreviewLabButtonSize
import me.tbsten.compose.preview.lab.ui.components.PreviewLabButtonStyle
import me.tbsten.compose.preview.lab.ui.components.PreviewLabIconButton
import me.tbsten.compose.preview.lab.ui.components.PreviewLabSurface
import me.tbsten.compose.preview.lab.ui.components.PreviewLabText
import me.tbsten.compose.preview.lab.ui.generated.resources.PreviewLabUiRes
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_close
import org.jetbrains.compose.resources.painterResource

/**
 * A single toast rendered by [ToastHost]: message, optional ghost action button and optional close button on a
 * [PreviewLabSurface] styled by [ToastDefaults]. Auto-dismisses after [ToastData.duration].
 */
@Composable
internal fun ToastItem(toast: ToastData, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
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
        val hasTrailingControls = toast.action != null || toast.showCloseButton

        PreviewLabSurface(
            shape = ToastDefaults.shape,
            color = containerColor,
            contentColor = contentColor,
            shadowElevation = ToastDefaults.shadowElevation,
            border = ToastDefaults.border(toast.type),
        ) {
            Row(
                modifier = Modifier
                    .heightIn(min = ToastDefaults.MinHeight)
                    .padding(
                        start = 16.dp,
                        top = 8.dp,
                        // buttons carry their own inner padding, so the trailing edge tightens up
                        end = if (hasTrailingControls) 8.dp else 16.dp,
                        bottom = 8.dp,
                    ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PreviewLabText(
                    text = toast.message,
                    color = contentColor,
                    modifier = Modifier.weight(1f, fill = false),
                )

                toast.action?.let { action ->
                    PreviewLabButton(
                        onClick = action.onClick,
                        style = PreviewLabButtonStyle.Ghost,
                        buttonSize = PreviewLabButtonSize.Small,
                        contentColor = contentColor,
                    ) {
                        PreviewLabText(text = action.label)
                    }
                }

                if (toast.showCloseButton) {
                    PreviewLabIconButton(
                        painter = painterResource(PreviewLabUiRes.drawable.icon_close),
                        contentDescription = "Close",
                        onClick = {
                            coroutineScope.launch {
                                isVisible = false
                                delay(ToastDefaults.AnimationDurationMillis)
                                onDismiss()
                            }
                        },
                        style = PreviewLabButtonStyle.Ghost,
                        tint = contentColor,
                        tooltip = null,
                    )
                }
            }
        }
    }
}
