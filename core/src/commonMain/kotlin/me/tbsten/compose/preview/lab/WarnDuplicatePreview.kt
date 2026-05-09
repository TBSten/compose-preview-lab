package me.tbsten.compose.preview.lab

/**
 * Emits a warning about duplicate preview ids to the platform's most discoverable
 * surface. Used internally by [distinctPreviewsById] to surface cross-artifact same-FQN
 * preview collisions; not part of the consumer-facing API.
 *
 * **Per-platform routing**:
 * - JVM / Android / iOS: stdout (`println`) — matches the historical behavior so existing
 *   build / test logs keep showing the warning.
 * - JS / Wasm JS: `console.warn` so the warning lights up in the browser DevTools (yellow
 *   highlighting) and ends up in CI runners that surface `console.warn` separately from
 *   `console.log`. Without this routing the warning was emitted via stdlib `println` →
 *   `console.log`, which is invisible to a user who has not opened DevTools and is also
 *   filtered by some headless test runners. See the corresponding KDoc on
 *   [distinctPreviewsById] for the broader visibility note.
 *
 * `commonMain` has no portable stderr API across all CMP targets — `expect/actual` is the
 * only way to route per-platform without dragging in a logging framework.
 */
@InternalComposePreviewLabApi
public expect fun warnDuplicatePreview(message: String)
