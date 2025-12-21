package me.tbsten.compose.preview.lab.previewlab.header

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import com.dokar.sonner.Toast
import com.dokar.sonner.ToastType
import com.dokar.sonner.ToasterState
import kotlinx.browser.window
import me.tbsten.compose.preview.lab.LocalPreviewLabPreview
import me.tbsten.compose.preview.lab.PreviewLabPreview
import me.tbsten.compose.preview.lab.previewlab.LocalToaster
import org.w3c.dom.url.URL

@Composable
internal actual fun copyUserHandler(params: Map<String, String>): CopyUserHandler {
    // TODO migrate LocalClipboard
    val clipboardManager = LocalClipboardManager.current
    val toaster = LocalToaster.current
    val currentPreview = LocalPreviewLabPreview.current

    val link = window.location.href
        .let {
            URL(it).apply {
                search = ""
                params.forEach { (key, value) ->
                    searchParams.set(key, value)
                }
            }.href
        }

    return CopyUserHandler(
        clipboardManager = clipboardManager,
        toaster = toaster,
        currentPreview = currentPreview,
        link = link,
    )
}

internal actual class CopyUserHandler(
    private val clipboardManager: ClipboardManager,
    private val toaster: ToasterState,
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
        toaster.show(
            Toast(
                message = buildString {
                    append("Copied ")
                    if (currentPreview != null) {
                        append("${currentPreview.displayName.split(".").last()} ")
                    }
                    append("url!")
                },
                type = ToastType.Success,
            ),
        )
    }
}
