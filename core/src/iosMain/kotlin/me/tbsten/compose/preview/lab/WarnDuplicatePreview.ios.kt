@file:OptIn(ExperimentalForeignApi::class)

package me.tbsten.compose.preview.lab

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSLog

@InternalComposePreviewLabApi
public actual fun warnDuplicatePreview(message: String) {
    // The format string is the fixed literal `"%@%@"` — it contains no embedded data,
    // so it cannot be turned into a printf-format-injection vector regardless of how
    // `WarnPrefix` or `message` evolve in the future. Both `WarnPrefix` (a `commonMain`
    // constant such as `"[ComposePreviewLab WARN] "`) and the user-supplied `message`
    // are passed as separate NSString arguments and consumed by the two `%@` specifiers
    // verbatim, so any `%`-prefixed sequences inside either value are rendered as
    // literal characters rather than re-interpreted as format directives. The default
    // NSLog severity is what Apple's tooling treats as "default"; the `WarnPrefix`
    // makes the line stand out in Xcode console / Console.app and keeps the body
    // identical to every other platform's emission for grep / log-collector filters.
    NSLog("%@%@", WarnPrefix as Any, message as Any)
}
