package me.tbsten.compose.preview.lab.previewlab.header

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import com.dokar.sonner.Toast
import com.dokar.sonner.ToastType
import kotlinx.browser.window
import me.tbsten.compose.preview.lab.LocalPreviewLabPreview
import me.tbsten.compose.preview.lab.previewlab.LocalToaster
import org.w3c.dom.url.URL

@Composable
internal actual fun copyUserHandler(): () -> Unit {
    // TODO migrate LocalClipboard
    val clipboardManager = LocalClipboardManager.current
    val toaster = LocalToaster.current
    val currentPreview = LocalPreviewLabPreview.current

    return {
        val link = window.location.href
            // set previewId
            .let {
                if (currentPreview != null) {
                    URL(it).apply {
                        searchParams.set("previewId", currentPreview.id)
                    }.href
                } else {
                    it
                }
            }

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
