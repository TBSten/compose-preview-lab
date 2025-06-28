package me.tbsten.compose.preview.lab.component

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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
internal fun SimpleModal(isVisible: Boolean, onDismissRequest: () -> Unit, content: @Composable () -> Unit) {
    if (isVisible) {
        Dialog(
            onDismissRequest = { onDismissRequest },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
            ),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures { onDismissRequest() }
                    }.padding(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    CommonIconButton(
                        painter = painterResource(Res.drawable.icon_close),
                        contentDescription = "close",
                        tint = PreviewLabTheme.colors.background,
                        onClick = onDismissRequest,
                    )
                }

                Box(
                    Modifier
                        .pointerInput(Unit) { detectTapGestures { } }
                        .clip(RoundedCornerShape(8.dp)),
                ) {
                    content()
                }
            }
        }
    }
}
