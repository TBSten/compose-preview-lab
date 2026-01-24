package me.tbsten.compose.preview.lab.previewlab.header

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboard
import me.tbsten.compose.preview.lab.ui.components.PreviewLabButton
import me.tbsten.compose.preview.lab.ui.components.PreviewLabModal

@Composable
internal fun Links() {
    var isShareDialogShow by remember { mutableStateOf(false) }

    val clipboard = LocalClipboard.current
    PreviewLabButton(
        text = "Copy link",
        onClick = {
            val url = "https://example.com"
        },
    )

    PreviewLabModal(
        isVisible = isShareDialogShow,
        onDismissRequest = { isShareDialogShow = false },
    ) {
    }
}
