package me.tbsten.compose.preview.lab.previewlab.header

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboard
import me.tbsten.compose.preview.lab.ui.components.Button
import me.tbsten.compose.preview.lab.ui.components.SimpleModal

@Composable
internal fun Links() {
    var isShareDialogShow by remember { mutableStateOf(false) }

    val clipboard = LocalClipboard.current
    Button(
        text = "Copy link",
        onClick = {
            val url = "https://example.com"
        },
    )

    SimpleModal(
        isVisible = isShareDialogShow,
        onDismissRequest = { isShareDialogShow = false },
    ) {
    }
}
