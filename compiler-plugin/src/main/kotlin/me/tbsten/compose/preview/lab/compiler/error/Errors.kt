package me.tbsten.compose.preview.lab.compiler.error

import me.tbsten.compose.preview.lab.compiler.error.ComposePreviewLabCompilerPluginError.Category
import org.jetbrains.kotlin.name.CallableId

// Concrete `ComposePreviewLabCompilerPluginError` implementations for the IR-side error
// sites enumerated in `.local/compiler-plugin-restructure/errors.md`.
//
// Each implementation takes the dynamic values it needs as constructor parameters,
// surfaces them through `context = contextOf { ... }`, and points at a `Replies.*`
// snippet for the "How to reply:" section.
//
// FIR-side diagnostics (`CollectScopeErrors.INVALID_COLLECT_SCOPE_VALUE` /
// `NON_LITERAL_COLLECT_SCOPE`) are *not* migrated here — they stay on the
// `KtDiagnosticFactory` track because the renderer chain is tightly coupled to the
// factory definition (see `errors.md` notes).

// -----------------------------------------------------------------------------
// IR ERROR — collect[All]ModulePreviews call-site issues
// -----------------------------------------------------------------------------

/**
 * `[ComposePreviewLab/IR,VERSION_GATE] collectAllModulePreviews() requires Kotlin 2.3.21
 * or later`. Fires when a `val previews by collectAllModulePreviews()` site is reached on
 * a Kotlin compiler that lacks the FIR per-declaration hint generator (< 2.3.20) or, for
 * KLIB targets, the KT-82395 fix (< 2.3.21).
 */
class UnsupportedCollectAllError(private val callName: String) : ComposePreviewLabCompilerPluginError {
    override val categories: List<Category> = listOf(Category.IR, Category.VERSION_GATE)
    override val message: String =
        "$callName() requires Kotlin 2.3.21 or later for cross-module preview aggregation"
    override val description: String =
        "Cross-module aggregation depends on (1) the FIR per-declaration hint generator " +
            "being registered (Kotlin 2.3.20+) for marker / hint declarations to exist on " +
            "dependency-module classpaths, and (2) on KLIB targets only, the IR `referenceFunctions` " +
            "walk being IC-safe (Kotlin 2.3.21+ for the KT-82395 fix). Neither gate is met for " +
            "this compilation."
    override val context: List<ContextEntry> = contextOf {
        +"call"(callName)
    }
    override val replies: List<String> = listOf(Replies.UpgradeKotlin2321)
}

/**
 * `[ComposePreviewLab/IR,INVALID_USAGE,PREVIEW_COLLECTION] collectModulePreviews() cannot
 * be used because collectPreviewsEnabled is false`. Fires per call site when the module
 * has `collectPreviewsEnabled = false` (PR #186 plugin option) but a
 * `collect[All]ModulePreviews()` call is still present.
 */
class CollectPreviewsDisabledError(private val callName: String) : ComposePreviewLabCompilerPluginError {
    override val categories: List<Category> =
        listOf(Category.IR, Category.INVALID_USAGE, Category.PREVIEW_COLLECTION)
    override val message: String =
        "$callName() cannot be used in this module because the `collectPreviewsEnabled` plugin option is false"
    override val description: String =
        "The disabled flag suppresses every per-declaration hint emission for this module " +
            "(and consequently every cross-module aggregation it might participate in), so any " +
            "call site is almost certainly a configuration mistake. Reporting an error keeps " +
            "users from observing a silently-empty list at runtime."
    override val context: List<ContextEntry> = contextOf {
        +"call"(callName)
        +"collectPreviewsEnabled"("false")
    }
    override val replies: List<String> = listOf(Replies.EnableCollectPreviews)
}

/**
 * `[ComposePreviewLab/IR,INVALID_USAGE,PREVIEW_COLLECTION] collectModulePreviews(scope = ...)
 * accepts only a compile-time string constant`. IR-side counterpart of the FIR
 * `NON_LITERAL_COLLECT_SCOPE` diagnostic — fires when the IR pass cannot fold `scope` to
 * an `IrConst<String>` (e.g. a non-`const` `val` reference that the FIR Checker had to
 * accept because it cannot distinguish `const val` from `val` at analysis time).
 */
class NonLiteralScopeIrError(private val callName: String) : ComposePreviewLabCompilerPluginError {
    override val categories: List<Category> =
        listOf(Category.IR, Category.INVALID_USAGE, Category.PREVIEW_COLLECTION)
    override val message: String =
        "$callName(scope = ...) accepts only a compile-time string constant"
    override val description: String =
        "The IR pass needs the value as an `IrConst<String>` so it can be embedded into " +
            "the synthetic `previewHint_<scope>` function name. The FIR " +
            "`CollectScopeCallChecker` rejects clear-cut non-literals (string concatenations, " +
            "function calls), but a plain (non-`const`) `val` reference cannot be told apart " +
            "from a `const val` at FIR time and is therefore left to this IR-pass check."
    override val context: List<ContextEntry> = contextOf {
        +"call"(callName)
    }
    override val replies: List<String> = listOf(Replies.LiteralScopeOnly)
}

/**
 * `[ComposePreviewLab/IR,INVALID_USAGE,PREVIEW_COLLECTION] collectModulePreviews(scope = "...")
 * is not a valid scope identifier`. IR-side counterpart of the FIR
 * `INVALID_COLLECT_SCOPE_VALUE` diagnostic — fires when a const-folded `IrConst<String>`
 * value fails the `[A-Za-z0-9_]+` regex (e.g. via
 * `private const val BAD = "has space"; collectModulePreviews(scope = BAD)` which bypasses
 * FIR analysis-time literal-value inspection).
 */
class InvalidScopeIrError(private val callName: String, private val scope: String) : ComposePreviewLabCompilerPluginError {
    override val categories: List<Category> =
        listOf(Category.IR, Category.INVALID_USAGE, Category.PREVIEW_COLLECTION)
    override val message: String =
        "$callName(scope = \"$scope\") is not a valid scope identifier"
    override val description: String =
        "The const-folded `IrConst<String>` value fails the scope identifier regex. The " +
            "FIR `CollectScopeCallChecker` catches inline string literals at analysis time " +
            "but a `const val` reference is indistinguishable from a non-`const` `val` " +
            "reference at FIR time and is therefore left to this IR-pass check."
    override val context: List<ContextEntry> = contextOf {
        +"call"(callName)
        +"scope"(scope)
    }
    override val replies: List<String> = listOf(Replies.ScopeFormatAllowed)
}

// -----------------------------------------------------------------------------
// IR ERROR — hint hash collision (PreviewLabIrGenerationExtension)
// -----------------------------------------------------------------------------

/**
 * `[ComposePreviewLab/IR,PREVIEW_COLLECTION] hint hash collision detected on '<hash>'`.
 * Fires when two distinct `@Preview` functions in the same module produce the same
 * SHA-256 truncated 7-byte hash that is used as the marker-interface short name suffix.
 * Astronomically unlikely at typical preview counts (~10⁻⁷ at 1k previews) but worth
 * surfacing because the resulting marker collision would silently break hint discovery.
 */
class HintHashCollisionError(private val hash: String, private val previewA: String, private val previewB: String) :
    ComposePreviewLabCompilerPluginError {
    override val categories: List<Category> = listOf(Category.IR, Category.PREVIEW_COLLECTION)
    override val message: String = "hint hash collision detected on '$hash'"
    override val description: String =
        "Two distinct @Preview functions hash to the same SHA-256 truncated 7-byte value. " +
            "This is astronomically rare (~10⁻⁷ at 1k previews); when it does happen the " +
            "marker-interface collision would silently break per-declaration hint discovery, " +
            "so the plugin surfaces it as an ERROR. Workaround: rename one of the functions " +
            "or its package."
    override val context: List<ContextEntry> = contextOf {
        +"hash"(hash)
        +"preview_a"(previewA)
        +"preview_b"(previewB)
    }
    override val replies: List<String> = listOf(Replies.Unknown)
}

// -----------------------------------------------------------------------------
// IR defensive — internal invariants that user source cannot normally trigger
// -----------------------------------------------------------------------------

/**
 * `[ComposePreviewLab/IR] PreviewExport class not found on the compilation classpath`.
 * Defensive: thrown when `pluginContext.referenceClass(PreviewExport)` returns `null`,
 * which would indicate a missing or version-mismatched runtime jar.
 */
class PreviewExportNotFoundError : ComposePreviewLabCompilerPluginError {
    override val categories: List<Category> = listOf(Category.IR)
    override val message: String = "PreviewExport class not found on the compilation classpath"
    override val description: String =
        "Expected `me.tbsten.compose.preview.lab.PreviewExport` to be resolvable through " +
            "the IR plugin context, but `referenceClass(...)` returned null. This usually " +
            "means the compose-preview-lab runtime / core dependency is missing or there is " +
            "a core / plugin version mismatch."
    override val replies: List<String> = listOf(Replies.Unknown)
}

/**
 * `[ComposePreviewLab/IR] <fqn> not found on the compilation classpath`. Defensive
 * thrown when `pluginContext.referenceFunctions(callableId)` returns empty for a
 * runtime function that the IR pass relies on (`lazyPreviewSequence` /
 * `distinctPreviewsByIdSequence`). Same shape for both call sites so a single Error
 * class covers both #9 and #10 from the error table.
 */
class RuntimeFunctionNotFoundError(private val callableId: CallableId) : ComposePreviewLabCompilerPluginError {
    override val categories: List<Category> = listOf(Category.IR)
    override val message: String =
        "${callableId.asSingleFqName().asString()} not found on the compilation classpath"
    override val description: String =
        "Expected the IR pass's `pluginContext.referenceFunctions(callableId)` to resolve " +
            "the listed callable, but it returned no candidates. This usually means the " +
            "compose-preview-lab runtime / core dependency is missing or there is a core / " +
            "plugin version mismatch."
    override val context: List<ContextEntry> = contextOf {
        +"callable"(callableId.asSingleFqName().asString())
    }
    override val replies: List<String> = listOf(Replies.Unknown)
}

/**
 * `[ComposePreviewLab/IR] collectModulePreviews/collectAllModulePreviews delegate must
 * be on a property with a getter`. Defensive: thrown when `property.getter` is `null`
 * for a `by collect[All]ModulePreviews()` site. By Kotlin's property model a non-getter
 * property cannot be observed in the IR pass, so this is an unreachable guard rather
 * than a user-facing diagnostic.
 */
class PropertyHasNoGetterError(private val callableId: CallableId) : ComposePreviewLabCompilerPluginError {
    override val categories: List<Category> = listOf(Category.IR)
    override val message: String =
        "${callableId.asSingleFqName().asString()} delegate must be on a property with a getter, " +
            "not a backing field"
    override val description: String =
        "Reached an `IrProperty` whose `getter` slot is null while transforming a " +
            "`by collect[All]ModulePreviews()` delegate. The Kotlin property model guarantees " +
            "every observable property has a getter, so this branch is a defensive guard."
    override val context: List<ContextEntry> = contextOf {
        +"callable"(callableId.asSingleFqName().asString())
    }
    override val replies: List<String> = listOf(Replies.Unknown)
}
