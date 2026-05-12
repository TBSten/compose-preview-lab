package me.tbsten.compose.preview.lab

import android.util.Log

private const val TAG: String = "ComposePreviewLab"

@InternalComposePreviewLabApi
public actual fun warnDuplicatePreview(message: String) {
    Log.w(TAG, "$WarnPrefix$message")
}
