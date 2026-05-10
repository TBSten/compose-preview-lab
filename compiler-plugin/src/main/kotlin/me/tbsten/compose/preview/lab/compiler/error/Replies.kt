package me.tbsten.compose.preview.lab.compiler.error

/**
 * Reusable reply / next-action snippets shared across error implementations.
 *
 * Centralising these snippets keeps wording consistent (e.g. the "upgrade Kotlin to
 * 2.3.21" advice should read the same regardless of which error surfaces it) and lets the
 * FIR-side `CollectScopeErrors` reference the same character class definition that the
 * IR-side `InvalidScopeIrError` does, preventing message drift between the two layers of
 * the regex defence.
 *
 * Constants follow PascalCase (`Unknown`, `UpgradeKotlin2321`, ...) to satisfy the
 * project's ktlint constant-naming rule (`.editorconfig` =>
 * `ktlint_property_naming_constant_naming = pascal_case`).
 */
object Replies {
    /**
     * Generic fallback for defensive / internal-invariant errors (`PreviewExportNotFoundError`,
     * `RuntimeFunctionNotFoundError`, `PropertyHasNoGetterError`, ...) — these signal an
     * unexpected runtime/classpath shape that the user normally cannot trigger from valid
     * source code, so the only useful reply is "file a bug".
     */
    const val Unknown: String =
        "This is an unexpected internal error. " +
            "Please report it at https://github.com/TBSten/compose-preview-lab/issues/new " +
            "with the surrounding compiler output."

    /**
     * Reply for `UnsupportedCollectAllError` (#3) — the user invoked
     * `collectAllModulePreviews()` on a Kotlin compiler that does not meet the
     * platform-dependent minimum version: JVM/Android need 2.3.20+ for the FIR
     * per-declaration hint generator; KLIB targets (JS / Wasm JS / Native)
     * additionally need 2.3.21+ for the KT-82395 `referenceFunctions` IC-safety fix.
     *
     * Wording matches `UnsupportedCollectAllError.description` so the two surfaces
     * stay in sync.
     */
    const val UpgradeKotlin2321: String =
        "Either upgrade the Kotlin compiler to the minimum version for your target " +
            "(2.3.20+ on JVM/Android or 2.3.21+ on KLIB targets such as JS / Wasm JS / Native), " +
            "or replace `collectAllModulePreviews()` with `collectModulePreviews()` to collect " +
            "only this module's @Preview functions."

    /**
     * Reply for `CollectPreviewsDisabledError` (#4) — the module has
     * `collectPreviewsEnabled = false` but a `collect[All]ModulePreviews()` call site is
     * still present.
     */
    const val EnableCollectPreviews: String =
        "Either remove the `collect[All]ModulePreviews()` call site from this module, or " +
            "re-enable preview collection in the Gradle DSL " +
            "(`composePreviewLab { collectPreviews { enabled.set(true) } }`) or via " +
            "`-P plugin:me.tbsten.compose.preview.lab:collectPreviewsEnabled=true` for " +
            "non-Gradle setups."

    /**
     * Reply for `InvalidScopeIrError` (#5b) — IR-side mirror of the FIR
     * `INVALID_COLLECT_SCOPE_VALUE` diagnostic. Wording matches
     * `CollectScopeErrors.INVALID_COLLECT_SCOPE_VALUE` renderer template so the two
     * defence layers stay in lockstep.
     */
    const val ScopeFormatAllowed: String =
        "Allowed characters: [A-Za-z0-9_]+ — the value is embedded into the synthetic " +
            "`previewHint_<scope>` function name, so any character outside the regex would " +
            "produce an invalid Kotlin identifier."

    /**
     * Reply for `NonLiteralScopeIrError` (#5a) — IR-side mirror of the FIR
     * `NON_LITERAL_COLLECT_SCOPE` diagnostic.
     */
    const val LiteralScopeOnly: String =
        "Pass a compile-time string constant for `scope = ...` — either an inline string " +
            "literal (`scope = \"design\"`) or a `const val` reference. Non-`const` vals, " +
            "string concatenations, and function calls cannot be embedded into the " +
            "synthetic identifier and are rejected."
}
