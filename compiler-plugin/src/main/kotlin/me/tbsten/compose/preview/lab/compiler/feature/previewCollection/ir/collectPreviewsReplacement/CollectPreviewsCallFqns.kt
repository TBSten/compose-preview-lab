package me.tbsten.compose.preview.lab.compiler.feature.previewCollection.ir.collectPreviewsReplacement

import org.jetbrains.kotlin.name.FqName

// Sentinel FQNs that the `collectPreviewsReplacement/` logic looks for in
// `val x by collect[All]ModulePreviews()` property delegates.
//
// Only the `ReplaceCollectPreviewsFunBody` IR transformer and the FIR-side
// `scopeValidation/CheckCollectScopeCall` walk these names — the FIR Checker matches
// call sites of these FQNs and validates their `scope = ...` argument, while the IR
// transformer locates the same call sites by FQN and rewrites the delegate.
//
// Kept inside `collectPreviewsReplacement/` (rather than a feature-wide root) because
// they are private to this logic: the rest of `previewCollection/` does not need to
// know what the user-facing sentinel call names are.

/** `me.tbsten.compose.preview.lab.collectModulePreviews` sentinel call FQN. */
internal val COLLECT_MODULE_PREVIEWS_FQN: FqName =
    FqName.fromSegments(listOf("me", "tbsten", "compose", "preview", "lab", "collectModulePreviews"))

/** `me.tbsten.compose.preview.lab.collectAllModulePreviews` sentinel call FQN. */
internal val COLLECT_ALL_MODULE_PREVIEWS_FQN: FqName =
    FqName.fromSegments(listOf("me", "tbsten", "compose", "preview", "lab", "collectAllModulePreviews"))
