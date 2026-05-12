@file:OptIn(ExperimentalForeignApi::class)

package me.tbsten.compose.preview.lab

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSLog

@InternalComposePreviewLabApi
public actual fun warnDuplicatePreview(message: String) {
    // The format string is `"$WarnPrefix%@"` — `WarnPrefix` is a `commonMain` literal
    // (`"[ComposePreviewLab WARN] "`) that contains no `%` characters, so it can be
    // safely concatenated into the NSLog format string without changing printf parsing.
    // `%@` is the only format specifier, and the user-supplied `message` is passed as
    // the corresponding NSString argument — so even if `message` contains `%`-prefixed
    // sequences, NSLog formats them as literal characters rather than consuming them.
    // The default NSLog severity is what Apple's tooling treats as "default"; the
    // `WarnPrefix` makes the line stand out in Xcode console / Console.app and keeps
    // the body identical to every other platform's emission for grep / log-collector
    // filters.
    NSLog("$WarnPrefix%@", message as Any)
}
