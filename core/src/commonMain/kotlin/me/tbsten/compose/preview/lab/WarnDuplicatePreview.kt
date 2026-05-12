package me.tbsten.compose.preview.lab

/**
 * Single source of truth prefix prepended by every [warnDuplicatePreview] actual
 * before the message body is emitted to the platform's warning surface.
 *
 * Keeping the prefix in `commonMain` ensures that grep / log-collector filters
 * configured against this exact literal keep working regardless of which platform
 * actually produced the line — JVM (`System.err`), Android (`Log.w`), iOS (`NSLog`)
 * and JS / Wasm JS (`console.warn`) all emit the same `"[ComposePreviewLab WARN] "`
 * prefix. The prefix is intentionally `internal const`: it is implementation detail
 * shared between the actual files, not part of the consumer-facing API.
 */
internal const val WarnPrefix: String = "[ComposePreviewLab WARN] "

/**
 * Emits a warning about duplicate preview ids to the platform's most discoverable
 * warning surface. Used internally by [distinctPreviewsById] to surface cross-artifact
 * same-FQN preview collisions; not part of the consumer-facing API.
 *
 * **Per-platform routing**:
 * - JVM: `System.err.println` — keeps the warning out of stdout (where build logs are
 *   parsed by tooling) and onto the standard error stream that IDEs / CI surfaces
 *   highlight separately.
 * - Android: `Log.w("ComposePreviewLab", ...)` — picks up Logcat's warning severity
 *   filter so the line is colored / level-tagged in Android Studio.
 * - iOS: `NSLog` — routes through the unified logging system so the line shows up in
 *   Xcode console and Console.app.
 * - JS / Wasm JS: `console.warn` — yellow-highlighted in browser DevTools and
 *   surfaced separately from `console.log` by headless test runners that filter logs
 *   by severity.
 *
 * All actuals prepend the shared [WarnPrefix] before [message] so callers and log
 * collectors can rely on a single literal across every platform.
 *
 * `commonMain` has no portable warning API across all CMP targets — `expect/actual` is
 * the only way to route per-platform without dragging in a logging framework.
 */
@InternalComposePreviewLabApi
public expect fun warnDuplicatePreview(message: String)
