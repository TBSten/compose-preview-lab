package me.tbsten.compose.preview.lab.compiler

import me.tbsten.compose.preview.lab.compiler.utils.classIdOf
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * Plugin-wide identifier and string constants — the Single Source of Truth for any FQN /
 * `ClassId` / `CallableId` / `Name` / `Regex` / pure-function helper that does **not**
 * require a `FirSession` to derive.
 *
 * # Boundary with `PreviewLabFirBuiltIns`
 *
 * `PreviewLabFirBuiltIns` is a `FirExtensionSessionComponent` and therefore tied to a
 * session lifetime. The dividing rule: **a definition belongs here whenever it can be
 * computed without a `FirSession` argument**. Session-bound helpers (e.g. anything that
 * reaches into `session.symbolProvider`, `session.predicateBasedProvider`, or stores
 * cross-extension caches) stays on `PreviewLabFirBuiltIns`.
 *
 * Concretely:
 * - `FqName` / `ClassId` / `CallableId` / `Name` / `Regex` literals → here.
 * - Pure functions over those identifiers (e.g. `hintFunctionCallableId(scope)`) → here.
 * - `FirSession`-scoped lazy caches (`hintEntries`, session config wrapper) →
 *   `PreviewLabFirBuiltIns`.
 *
 * When in doubt prefer this file over `PreviewLabFirBuiltIns`: anything resolvable from
 * here is also resolvable from there (via plain `import`), but the reverse adds an
 * unnecessary session-component hop. The previous flat layout exposed these constants on
 * `PreviewLabFirBuiltIns.Companion`; this file is what callers should now reach for.
 */
internal object PreviewLabConstants {

    /**
     * Package that owns every per-declaration hint function and marker interface.
     *
     * Cross-module discovery walks
     * `IrPluginContext.referenceFunctions(CallableId(HINT_PACKAGE, "previewHint_<scope>"))`,
     * so consumers learn about dependency-module previews purely by the package + per-scope
     * callable name — no per-hint inspection needed.
     */
    val HINT_PACKAGE: FqName = FqName("me.tbsten.compose.preview.lab.hints")

    // -----------------------------------------------------------------------------
    // Sentinel call FQNs / CallableIds (collect[All]ModulePreviews)
    // -----------------------------------------------------------------------------

    /** `me.tbsten.compose.preview.lab.collectModulePreviews` sentinel call FQN. */
    val COLLECT_MODULE_PREVIEWS_FQN: FqName =
        FqName.fromSegments(listOf("me", "tbsten", "compose", "preview", "lab", "collectModulePreviews"))

    /** `me.tbsten.compose.preview.lab.collectAllModulePreviews` sentinel call FQN. */
    val COLLECT_ALL_MODULE_PREVIEWS_FQN: FqName =
        FqName.fromSegments(listOf("me", "tbsten", "compose", "preview", "lab", "collectAllModulePreviews"))

    // -----------------------------------------------------------------------------
    // `@Preview` annotation FQNs (CMP / Android variants)
    // -----------------------------------------------------------------------------

    /** CMP `@Preview` annotation FQN (used for predicate registration). */
    val CMP_PREVIEW_ANNOTATION_FQN: FqName =
        FqName("org.jetbrains.compose.ui.tooling.preview.Preview")

    /** Android `@Preview` annotation FQN (used for predicate registration). */
    val ANDROID_PREVIEW_ANNOTATION_FQN: FqName =
        FqName("androidx.compose.ui.tooling.preview.Preview")

    // -----------------------------------------------------------------------------
    // `@ComposePreviewLabOption` identifiers + argument names
    // -----------------------------------------------------------------------------

    /**
     * `me.tbsten.compose.preview.lab.ComposePreviewLabOption` FQN — used both for FIR
     * `LookupPredicate` registration (eagerly resolves the annotation type ref) and as
     * the source of [COMPOSE_PREVIEW_LAB_OPTION_CLASS_ID].
     */
    val COMPOSE_PREVIEW_LAB_OPTION_FQN: FqName =
        FqName("me.tbsten.compose.preview.lab.ComposePreviewLabOption")

    /**
     * `me.tbsten.compose.preview.lab.ComposePreviewLabOption` `ClassId` — looked up on
     * each `@Preview` symbol to read user-supplied options (e.g. `ignore = true`) during
     * hint emission.
     */
    val COMPOSE_PREVIEW_LAB_OPTION_CLASS_ID: ClassId =
        ClassId.topLevel(COMPOSE_PREVIEW_LAB_OPTION_FQN)

    /** `@ComposePreviewLabOption(ignore = ...)` argument name. */
    val IGNORE_NAME: Name = Name.identifier("ignore")

    /** `@ComposePreviewLabOption(collectScopes = ...)` argument name. */
    val COLLECT_SCOPE_NAME: Name = Name.identifier("collectScopes")

    // -----------------------------------------------------------------------------
    // `CollectedPreview` / hint naming
    // -----------------------------------------------------------------------------

    /** `me.tbsten.compose.preview.lab.CollectedPreview` `ClassId` — return type of every hint function. */
    val COLLECTED_PREVIEW_CLASS_ID: ClassId = classIdOf("me.tbsten.compose.preview.lab", "CollectedPreview")

    /**
     * Prefix of every per-declaration hint function. Full name is `previewHint_<scope>`,
     * where `<scope>` is the value of `@ComposePreviewLabOption(collectScopes = ...)` (or
     * `"default"` when not set). Encoding the scope into the function name lets the IR
     * side filter hints purely by `referenceFunctions(CallableId(HINT_PACKAGE, name))` —
     * no per-hint inspection is needed.
     */
    const val PreviewHintFunctionPrefix: String = "previewHint_"

    /**
     * Prefix of the per-`@Preview` marker interface name. Full name is
     * `PreviewHintMarker_<sanitized_fqn>_<hash>` where `<sanitized_fqn>` is the source
     * FQN with `.` replaced by `_` (debugging aid) and `<hash>` is the canonical-key
     * sha256 ([HashLength] chars, used for overload disambiguation and as the
     * cross-FIR/IR matching key).
     */
    const val PreviewHintMarkerPrefix: String = "PreviewHintMarker_"

    /** Length of the hash suffix on marker / hint declarations (matches `computeHintHash`). */
    const val HashLength: Int = 8

    /**
     * Validation rule for `collectScope` values.
     *
     * The scope is embedded directly into a Kotlin identifier (`previewHint_<scope>`),
     * so anything outside `[A-Za-z0-9_]+` would either fail to compile or, worse,
     * accidentally land on an unrelated function name. Validating up front lets us
     * surface the mistake as a clear diagnostic instead.
     */
    val SCOPE_VALIDATION_REGEX: Regex = Regex("[A-Za-z0-9_]+")

    /**
     * Builds the hint function callable id for a given scope. The hint function name
     * always lives in [HINT_PACKAGE], so per-scope discovery only needs to vary the
     * callable name suffix.
     *
     * **Sample call**: `hintFunctionCallableId("design")` →
     * `CallableId(FqName("me.tbsten.compose.preview.lab.hints"), Name.identifier("previewHint_design"))`
     */
    fun hintFunctionCallableId(scope: String): CallableId =
        CallableId(HINT_PACKAGE, Name.identifier(PreviewHintFunctionPrefix + scope))
}
