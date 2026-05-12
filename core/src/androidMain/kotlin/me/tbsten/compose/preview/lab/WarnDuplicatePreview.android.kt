package me.tbsten.compose.preview.lab

import android.util.Log

private const val TAG: String = "ComposePreviewLab"

@InternalComposePreviewLabApi
public actual fun warnDuplicatePreview(message: String) {
    // `Log.w` routes through Logcat's WARN severity so Android Studio colors/filters
    // the line as a warning. The Logcat tag (`"ComposePreviewLab"`) already provides
    // a per-line filter; we additionally prepend the shared [WarnPrefix] so the body
    // is grep-identical to the other platforms (JVM / iOS / JS / Wasm JS), allowing
    // log-collectors configured against `"[ComposePreviewLab WARN] "` to match here
    // too.
    Log.w(TAG, "$WarnPrefix$message")
}
