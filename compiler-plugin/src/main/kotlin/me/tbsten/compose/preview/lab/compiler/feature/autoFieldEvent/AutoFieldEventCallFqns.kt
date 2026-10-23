package me.tbsten.compose.preview.lab.compiler.feature.autoFieldEvent

import org.jetbrains.kotlin.name.FqName

// Sentinel FQNs that the `autoFieldEvent/` IR transformer looks for in
// `someParam = autoField()` / `someParam = autoEvent()` call sites.
//
// Only the `InjectAutoLabelIrTransformer` walks these names — it locates
// matching `IrCall` value arguments and rewrites their `label` parameter to
// embed the surrounding parameter's name. Kept inside `autoFieldEvent/`
// (rather than a feature-wide root) because they are private to this logic.

/** `me.tbsten.compose.preview.lab.previewlab.autoField` sentinel call FQN. */
internal val AUTO_FIELD_FQN: FqName =
    FqName.fromSegments(listOf("me", "tbsten", "compose", "preview", "lab", "previewlab", "autoField"))

/** `me.tbsten.compose.preview.lab.previewlab.autoEvent` sentinel call FQN. */
internal val AUTO_EVENT_FQN: FqName =
    FqName.fromSegments(listOf("me", "tbsten", "compose", "preview", "lab", "previewlab", "autoEvent"))

/** The name of the `label` parameter on both [AUTO_FIELD_FQN] and [AUTO_EVENT_FQN]. */
internal const val AutoLabelParameterName: String = "label"
