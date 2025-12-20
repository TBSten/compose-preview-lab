package me.tbsten.compose.preview.lab.previewlab.header

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.tbsten.compose.preview.lab.ui.components.Button
import me.tbsten.compose.preview.lab.ui.components.ButtonVariant

@Composable
internal actual fun RowScope.PlatformHeaders() {
    Spacer(Modifier.weight(1f))

    val copyUrl = copyUserHandler()

    Button(
        text = "Copy",
        variant = ButtonVariant.PrimaryOutlined,
        onClick = { copyUrl() },
        modifier = Modifier.align(Alignment.CenterVertically),
    )
}

@Composable
internal expect fun copyUserHandler(): () -> Unit
