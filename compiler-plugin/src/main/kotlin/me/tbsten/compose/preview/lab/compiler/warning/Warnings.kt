package me.tbsten.compose.preview.lab.compiler.warning

import me.tbsten.compose.preview.lab.compiler.error.ComposePreviewLabCompilerPluginError.Category
import me.tbsten.compose.preview.lab.compiler.error.Replies

// Concrete `ComposePreviewLabCompilerPluginWarning` implementations for the IR-side
// warning sites enumerated in
// `.local/ticket/compiler-plugin-restructure-4-warning-expansion.md`.
//
// Each implementation takes the dynamic values it needs as constructor parameters,
// surfaces them through `context = contextOf { ... }`, and points at a `Replies.*`
// snippet for the "How to reply:" section. The reply constants are reused from the
// shared `error/Replies.kt` (re-exported via the top-level `Replies` typealias) so
// wording stays consistent across error / warning surfaces.

// -----------------------------------------------------------------------------
// IR WARNING — discoverHints: namespace squatting / cross-artifact duplicates
// -----------------------------------------------------------------------------

/**
 * `[ComposePreviewLab/IR,PREVIEW_COLLECTION] a function in
 * 'me.tbsten.compose.preview.lab.hints' matching the per-scope hint shape is missing
 * the @SyntheticPreviewHint marker`. Fires from `discoverHints` (IR pass) when a
 * candidate has the right structural shape (correct package, marker-shape parameter,
 * `CollectedPreview` return type, name pattern) but lacks the plugin-stamped
 * `@SyntheticPreviewHint` annotation.
 *
 * The candidate is filtered out of the aggregated preview list to prevent a
 * third-party library from squatting the reserved `me.tbsten.compose.preview.lab.hints`
 * package and injecting previews into the consumer's `collectAllModulePreviews()`
 * result. Processing continues with the remaining authentic hints — the warning
 * surfaces the dropped candidate so users can investigate the dependency that owns it.
 *
 * **Sample rendered output**:
 * ```
 * [ComposePreviewLab/IR,PREVIEW_COLLECTION] a function in
 * 'me.tbsten.compose.preview.lab.hints' matching the per-scope hint shape is missing
 * the @SyntheticPreviewHint marker — candidate dropped to prevent namespace squatting
 *
 *     Only the compose-preview-lab compiler plugin should emit declarations into the
 *     reserved hint package. A function in this package that lacks the
 *     `@me.tbsten.compose.preview.lab.SyntheticPreviewHint` marker is treated as a
 *     squatter and dropped from the aggregated preview list. ...
 *
 *   Context:
 *     hint_package: me.tbsten.compose.preview.lab.hints
 *     marker: me.tbsten.compose.preview.lab.hints.PreviewHintMarker_squatter_Fake_deadbeef
 * ```
 */
class HintNamespaceSquattingWarning(private val packageName: String, private val markerFqn: String) :
    ComposePreviewLabCompilerPluginWarning {
    override val categories: List<Category> = listOf(Category.IR, Category.PREVIEW_COLLECTION)
    override val message: String =
        "a function in '$packageName' matching the per-scope hint shape is missing the @SyntheticPreviewHint marker " +
            "(marker parameter '$markerFqn') — candidate dropped to prevent namespace squatting"
    override val description: String =
        "Only the compose-preview-lab compiler plugin should emit declarations into the reserved hint package. " +
            "A function in this package that lacks the `@me.tbsten.compose.preview.lab.SyntheticPreviewHint` " +
            "marker is treated as a squatter and dropped from the aggregated preview list — the structural " +
            "shape alone (correct package, marker-shape parameter, `CollectedPreview` return type, name " +
            "pattern) is not enough to be considered authentic."
    override val context: List<String> = contextOf {
        "hint_package"(packageName)
        "marker"(markerFqn)
    }
    override val replies: List<String> = listOf(Replies.InvestigateHintsPackageOwner)
}

/**
 * `[ComposePreviewLab/IR,PREVIEW_COLLECTION] N synthetic hint functions on the
 * classpath share marker '<fqn>' (scope = '<scope>')`. Fires from `discoverHints`
 * (IR pass) when `referenceFunctions(previewHint_<scope>)` returns two or more
 * authentic hints (passed the `@SyntheticPreviewHint` squatting guard) sharing the
 * same marker FQN — i.e. the same source `@Preview` signature is reachable through
 * multiple dependency-graph paths.
 *
 * Runtime `distinctPreviewsById` already collapses the duplicates, so processing
 * continues; the warning surfaces a likely dependency-graph misconfiguration (e.g.
 * the same library republished under two coordinates, two versions on the classpath,
 * or an incremental-compile partial state surfacing both the cached and the rebuilt
 * symbol).
 *
 * Note: in practice this is hard to trigger end-to-end because both the JVM
 * classloader and the KLIB linker collapse same-FQN duplicates down to a single
 * symbol before `referenceFunctions` returns. The warning is kept for unusual setups
 * where the symbol provider surfaces both declarations and to make the intent
 * explicit; the runtime `distinctPreviewsById` is the user-visible signal in the
 * common case.
 *
 * **Sample rendered output**:
 * ```
 * [ComposePreviewLab/IR,PREVIEW_COLLECTION] 2 synthetic hint functions on the
 * classpath share marker 'me.tbsten.compose.preview.lab.hints.PreviewHintMarker_uilib_default_MyButton_abcd123'
 * (scope = 'default') — runtime distinctPreviewsById will keep the first occurrence
 *
 *   Context:
 *     count: 2
 *     marker: me.tbsten.compose.preview.lab.hints.PreviewHintMarker_uilib_default_MyButton_abcd123
 *     scope: default
 * ```
 */
class CrossArtifactHintDuplicateWarning(private val count: Int, private val markerFqn: String, private val scope: String) :
    ComposePreviewLabCompilerPluginWarning {
    init {
        // Invariant: this warning only fires after `.filterValues { it.size > 1 }`
        // in DiscoverHints.kt, so callers must pass at least 2. Guarding here keeps
        // the wording "$count synthetic hint functions ... share marker" honest
        // (singular "1" would be a misleading regression).
        require(count >= 2) { "count must be >= 2 (got $count)" }
    }

    override val categories: List<Category> = listOf(Category.IR, Category.PREVIEW_COLLECTION)
    override val message: String =
        "$count synthetic hint functions on the classpath share marker '$markerFqn' (scope = '$scope') — " +
            "runtime `distinctPreviewsById` will keep the first occurrence"
    override val description: String =
        "The same `@Preview` source is reachable through multiple dependency-graph paths, so the per-scope " +
            "hint stub is provided by two or more artifacts simultaneously. Runtime " +
            "`distinctPreviewsById` retains only the first occurrence, but the over-pull usually points at " +
            "a dependency-graph misconfiguration worth investigating."
    override val context: List<String> = contextOf {
        "count"(count.toString())
        "marker"(markerFqn)
        "scope"(scope)
    }
    override val replies: List<String> = listOf(Replies.InvestigateDuplicateArtifacts)
}
