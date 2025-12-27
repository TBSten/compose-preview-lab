package me.tbsten.compose.preview.lab.previewlab.header

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import me.tbsten.compose.preview.lab.ui.components.toast.ToastHostState
import me.tbsten.compose.preview.lab.ui.components.toast.ToastType
import kotlinx.browser.window
import me.tbsten.compose.preview.lab.LocalPreviewLabPreview
import me.tbsten.compose.preview.lab.PreviewLabPreview
import me.tbsten.compose.preview.lab.previewlab.LocalToastHostState
import org.w3c.dom.url.URL

@Composable
internal actual fun copyUserHandler(params: Map<String, String>): CopyUserHandler {
    // TODO migrate LocalClipboard
    val clipboardManager = LocalClipboardManager.current
    val toastHostState = LocalToastHostState.current
    val currentPreview = LocalPreviewLabPreview.current

    val link = window.location.href
        .let {
            println()
            URL(it).apply {
                search = ""
                params.forEach { (key, value) ->
                    searchParams.set(key, value)
                }
            }.href
        }

    return CopyUserHandler(
        clipboardManager = clipboardManager,
        toastHostState = toastHostState,
        currentPreview = currentPreview,
        link = link,
    )
}

internal actual class CopyUserHandler(
    private val clipboardManager: ClipboardManager,
    private val toastHostState: ToastHostState,
    private val currentPreview: PreviewLabPreview?,
    actual val link: String,
) {
    actual operator fun invoke() {
        clipboardManager.setText(
            buildAnnotatedString {
                withLink(LinkAnnotation.Url(link)) {
                    append(link)
                }
            },
        )
        toastHostState.show(
            message = buildString {
                append("Copied ")
                if (currentPreview != null) {
                    append("${currentPreview.displayName.split(".").last()} ")
                }
                append("url!")
            },
            type = ToastType.Success,
        )
    }
}
