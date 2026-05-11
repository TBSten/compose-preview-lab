package me.tbsten.compose.preview.lab.compiler.error

/**
 * Top-level alias for [ComposePreviewLabCompilerPluginError.Replies] — callers in
 * `Errors.kt` / `Warnings.kt` get the same short `Replies.Unknown` / `Replies.UpgradeKotlin2321`
 * form without an explicit `import` of the nested type. The reply constants themselves live
 * inside the `Error` interface so they sit alongside the interface they serve (see review
 * thread #6 on PR #199).
 */
typealias Replies = ComposePreviewLabCompilerPluginError.Replies
