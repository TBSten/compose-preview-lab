package me.tbsten.compose.preview.lab.previewlab.header

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.tbsten.compose.preview.lab.ui.components.Button
import me.tbsten.compose.preview.lab.ui.components.ButtonVariant
import me.tbsten.compose.preview.lab.ui.components.Divider

@Composable
internal actual fun RowScope.PlatformHeaders() {
    Divider()

    val copyUrl = copyUserHandler()

    Row(
        Modifier
            .align(Alignment.CenterVertically)
            .height(IntrinsicSize.Min),
    ) {
        Button(
            text = "Copy link",
            variant = ButtonVariant.PrimaryOutlined,
            onClick = { copyUrl() },
            modifier = Modifier.fillMaxHeight(),
        )
    }
}

@Composable
internal expect fun copyUserHandler(): () -> Unit
