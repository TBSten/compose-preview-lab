@file:OptIn(ExperimentalForeignApi::class)

package me.tbsten.compose.preview.lab

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSLog

@InternalComposePreviewLabApi
public actual fun warnDuplicatePreview(message: String) {
    // `NSLog("%@", ...)` formats the message as an NSString without consuming any
    // `%`-prefixed sequences inside the body — `%@` is the only specifier in the
    // format string, so printf-style injection from user-supplied content is safe.
    // The default NSLog severity level is what Apple's tooling treats as "default";
    // we prefix with `[WARN]` so the line stands out in Xcode console / Console.app.
    NSLog("[ComposePreviewLab WARN] %@", message as Any)
}
