package me.tbsten.compose.preview.lab.compiler.feature.previewCollection

import me.tbsten.compose.preview.lab.compiler.PreviewLabConstants
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name

// Single source of truth for the hint function callable name (`previewHint_<scope>`).
//
// The hint function name encodes the collection scope so that cross-module discovery
// can filter purely by `IrPluginContext.referenceFunctions(CallableId(HINT_PACKAGE, name))`
// — no per-hint inspection is needed.
//
// - Generation side (FIR `GeneratePreviewHintFir`) emits one
//   `previewHint_<scope>(value: PreviewHintMarker_<sanitized_fqn>_<hash>?): CollectedPreview`
//   overload per scope.
// - Discovery side (IR `DiscoverHints`) looks up
//   `CallableId(HINT_PACKAGE, Name.identifier("previewHint_<scope>"))` for the requested
//   scope.
//
// Both sides go through the helpers in this file so the name format cannot drift.

/**
 * Builds the hint function callable id for [scope].
 *
 * **Sample call**: `hintFunctionCallableId("design")`
 *
 * **Result**: `CallableId(FqName("me.tbsten.compose.preview.lab.hints"), Name.identifier("previewHint_design"))`
 *
 * Convenience wrapper around [PreviewLabConstants.hintFunctionCallableId] kept close to the
 * other naming helpers in this feature directory; both paths return identical values.
 */
internal fun hintFunctionCallableId(scope: String): CallableId = PreviewLabConstants.hintFunctionCallableId(scope)

/**
 * Whether a candidate top-level callable name matches the synthesized hint function
 * pattern (i.e. starts with `previewHint_`).
 *
 * **Sample**:
 * - `isHintFunctionName(Name.identifier("previewHint_default"))` → `true`
 * - `isHintFunctionName(Name.identifier("renderPreview"))` → `false`
 *
 * The check is structural only — it does not validate that the suffix is a legal scope
 * identifier. Pair with [PreviewLabConstants.SCOPE_VALIDATION_REGEX] when a strict match
 * is required (e.g. discovery-side filtering of arbitrary classpath callables).
 */
internal fun isHintFunctionName(name: Name): Boolean = name.asString().startsWith(PreviewLabConstants.PreviewHintFunctionPrefix)
