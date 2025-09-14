package me.tbsten.compose.preview.lab.component

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import me.tbsten.compose.preview.lab.core.generated.resources.Res
import me.tbsten.compose.preview.lab.core.generated.resources.icon_close
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun SimpleModal(
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    content: @Composable () -> Unit,
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = { onDismissRequest },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
            ),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures { onDismissRequest() }
                    }.fillMaxSize(),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .widthIn(min = 200.dp, max = 600.dp)
                        .padding(contentPadding),
                ) {
                    CommonIconButton(
                        painter = painterResource(Res.drawable.icon_close),
                        contentDescription = "close",
                        tint = PreviewLabTheme.colors.background,
                        onClick = onDismissRequest,
                        modifier = Modifier.align(Alignment.End),
                    )

                    Box(
                        Modifier
                            .disableTapEvents()
                            .clip(RoundedCornerShape(8.dp)),
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

private fun Modifier.disableTapEvents() = pointerInput(Unit) { detectTapGestures { } }
