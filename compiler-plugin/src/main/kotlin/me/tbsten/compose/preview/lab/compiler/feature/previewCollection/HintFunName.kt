package me.tbsten.compose.preview.lab.compiler.feature.previewCollection

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
 * Prefix of every per-declaration hint function. Full name is `previewHint_<scope>`,
 * where `<scope>` is the value of `@ComposePreviewLabOption(collectScopes = ...)` (or
 * `"default"` when not set). Encoding the scope into the function name lets the IR
 * side filter hints purely by `referenceFunctions(CallableId(HINT_PACKAGE, name))` —
 * no per-hint inspection is needed.
 */
internal const val PreviewHintFunctionPrefix: String = "previewHint_"

/**
 * Builds the hint function callable id for [scope].
 *
 * **Sample call**: `hintFunctionCallableId("design")`
 *
 * **Result**: `CallableId(FqName("me.tbsten.compose.preview.lab.hints"), Name.identifier("previewHint_design"))`
 */
internal fun hintFunctionCallableId(scope: String): CallableId =
    CallableId(HINT_PACKAGE, Name.identifier(PreviewHintFunctionPrefix + scope))

/**
 * Whether a candidate top-level callable name matches the synthesized hint function
 * pattern (i.e. starts with `previewHint_`).
 *
 * **Sample**:
 * - `isHintFunctionName(Name.identifier("previewHint_default"))` → `true`
 * - `isHintFunctionName(Name.identifier("renderPreview"))` → `false`
 *
 * The check is structural only — it does not validate that the suffix is a legal scope
 * identifier. Pair with `SCOPE_VALIDATION_REGEX` when a strict match is required (e.g.
 * discovery-side filtering of arbitrary classpath callables).
 */
internal fun isHintFunctionName(name: Name): Boolean = name.asString().startsWith(PreviewHintFunctionPrefix)
