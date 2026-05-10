package me.tbsten.compose.preview.lab.compiler.error

/**
 * Structured representation of an error reported by the compose-preview-lab compiler plugin.
 *
 * The interface is intentionally **not** an `Exception` — it is a pure data model that can
 * be either:
 * - reported through [org.jetbrains.kotlin.cli.common.messages.MessageCollector] with the
 *   `MessageCollector.report(error, location)` extension defined in `ReportError.kt`, or
 * - thrown by wrapping it inside [ComposePreviewLabCompilerPluginException] via
 *   [throwAsException] (for defensive `error("...")` ports).
 *
 * Concrete error implementations live in `Errors.kt`. Each implementation receives the
 * dynamic values it needs (FQN, hash, version, ...) as constructor parameters, and
 * surfaces them through [context] using the `contextOf { "label"(value) }` DSL so the
 * rendered error message follows a consistent shape (see `ReportError.kt::buildErrorBody`).
 *
 * **Sample rendered output** (from `ReportError.kt::buildErrorBody`):
 * ```
 * [ComposePreviewLab/IR,PREVIEW_COLLECTION] hint hash collision detected on 'abc1234'
 *
 *     Two distinct @Preview functions hash to the same value.
 *
 *   Context:
 *     hash: abc1234
 *     preview_a: com.example.A(kotlin.Int)
 *     preview_b: com.example.B(kotlin.String)
 *
 *   How to reply:
 *     This is an unknown error. Please report it to https://github.com/...
 * ```
 */
interface ComposePreviewLabCompilerPluginError {
    /**
     * Categories to embed into the rendered `[ComposePreviewLab/<categories>]` prefix.
     *
     * Joined with `,` (no spaces) by the renderer. Order is preserved so that the most
     * coarse-grained category (e.g. `IR` / `FIR`) typically comes first followed by
     * feature-axis tags (`PREVIEW_COLLECTION`, `INVALID_USAGE`, ...).
     */
    val categories: List<Category>

    /** Single-line summary headline shown right after the prefix. No trailing period. */
    val message: String

    /**
     * Optional multi-line static explanation of why this error happens and how it is
     * detected. Renderer indents each line by 4 spaces.
     */
    val description: String? get() = null

    /**
     * Ordered list of pre-rendered context lines. Renderer prints each verbatim under a
     * "Context:" header. Build the list with the [ErrorContextBuilder] DSL via
     * [contextOf]:
     *
     * ```kotlin
     * override val context = contextOf {
     *     "hash"(hash)            // → "hash: <hash>"
     *     "preview_a"(previewA)   // → "preview_a: <previewA>"
     *     "isVersionGated"()      // → "isVersionGated" (boolean flag form)
     * }
     * ```
     *
     * Same `label` may appear multiple times (e.g. two `"preview"` entries for the two
     * collided previews). The receiving type is `List<String>` rather than a dedicated
     * data class so callers can mix `label: value` entries with boolean-tag entries
     * without a paired `boolean` field per row.
     */
    val context: List<String> get() = emptyList()

    /**
     * Ordered list of human-readable suggested replies / next actions. Renderer prints
     * each under a "How to reply:" header, indented by 4 spaces and respecting embedded
     * newlines.
     *
     * Empty list is allowed for errors with no actionable reply.
     */
    val replies: List<String>

    /**
     * Coarse-grained categorisation that drives the `[ComposePreviewLab/<categories>]`
     * renderer prefix.
     *
     * - [FIR] / [IR] — compiler phase axis. Mutually exclusive within a single error
     *   instance (FIR diagnostics are managed separately via `KtDiagnosticFactory`; this
     *   axis is mostly `IR` in Ticket 0).
     * - [PREVIEW_COLLECTION] / [TRANSFORM_PRIVATE_PREVIEW_TO_INTERNAL] — feature axis.
     *   Matches the `feature/<feature-name>/` package roots planned for Ticket 1.
     * - [INVALID_USAGE] / [VERSION_GATE] — error-cause axis. Used to tag user-facing
     *   misuse vs. environment-level gating failures.
     */
    enum class Category {
        FIR,
        IR,
        PREVIEW_COLLECTION,
        TRANSFORM_PRIVATE_PREVIEW_TO_INTERNAL,
        INVALID_USAGE,
        VERSION_GATE,
    }

    /**
     * Reusable reply / next-action snippets shared across error implementations.
     *
     * Centralising these snippets keeps wording consistent and lets the FIR-side
     * `CollectScopeErrors` reference the same character class definition that the
     * IR-side `InvalidScopeIrError` does, preventing message drift between the two layers
     * of the regex defence.
     *
     * Nested inside [ComposePreviewLabCompilerPluginError] so error implementations can
     * reach the reply snippets with the unqualified `Replies.*` form; callers that prefer
     * the short form can `import ComposePreviewLabCompilerPluginError.Replies` at the
     * top of `Errors.kt`. The companion alias [me.tbsten.compose.preview.lab.compiler.error.Replies]
     * (declared in `Replies.kt`) is a top-level typealias for callers that want the
     * shortest possible form.
     */
    object Replies {
        /**
         * Generic fallback for defensive / internal-invariant errors. Used when the user
         * cannot reasonably trigger the error from valid source code.
         */
        const val Unknown: String =
            "This is an unexpected internal error. " +
                "Please report it at https://github.com/TBSten/compose-preview-lab/issues/new " +
                "with the surrounding compiler output."

        /**
         * Reply for `UnsupportedCollectAllError` — the user invoked
         * `collectAllModulePreviews()` on a Kotlin compiler that does not meet the
         * platform-dependent minimum version: JVM/Android need 2.3.20+ for the FIR
         * per-declaration hint generator; KLIB targets (JS / Wasm JS / Native)
         * additionally need 2.3.21+ for the KT-82395 `referenceFunctions` IC-safety fix.
         *
         * Includes a `collectModulePreviews()` fallback snippet so users get a copy-pasteable
         * downgrade path instead of being forced to upgrade Kotlin.
         */
        val UpgradeKotlin2321: String =
            """
            Either upgrade the Kotlin compiler to the minimum version for your target
            (2.3.20+ on JVM/Android or 2.3.21+ on KLIB targets such as JS / Wasm JS / Native),
            or replace `collectAllModulePreviews()` with `collectModulePreviews()` to collect
            only this module's @Preview functions:

              // your-feature.kt
              val previews by collectModulePreviews()
            """.trimIndent()

        /**
         * Reply for `CollectPreviewsDisabledError` — the module has the
         * `composePreviewLab.collectPreviews.enabled` Gradle option set to `false` but a
         * `collect[All]ModulePreviews()` call site is still present.
         *
         * Includes the full `composePreviewLab { collectPreviews { enabled.set(true) } }`
         * Gradle DSL snippet so users get a copy-pasteable fix.
         */
        val EnableCollectPreviews: String =
            """
            Either remove the `collect[All]ModulePreviews()` call site from this module, or
            re-enable preview collection in the Gradle DSL:

              // build.gradle.kts
              composePreviewLab {
                  collectPreviews {
                      enabled.set(true)
                  }
              }
            """.trimIndent()

        /**
         * Reply for `InvalidScopeIrError` — IR-side mirror of the FIR
         * `INVALID_COLLECT_SCOPE_VALUE` diagnostic. Wording matches
         * `CollectScopeErrors.INVALID_COLLECT_SCOPE_VALUE` renderer template so the two
         * defence layers stay in lockstep.
         *
         * Includes the `@ComposePreviewLabOption(collectScopes = [...])` annotation form
         * users should adopt instead of passing an invalid string.
         */
        val ScopeFormatAllowed: String =
            """
            Allowed characters: [A-Za-z0-9_]+ — the value is embedded into the synthetic
            `previewHint_<scope>` function name, so any character outside the regex would
            produce an invalid Kotlin identifier. Use a sanitized scope identifier on both
            the @Preview side and the collect call:

              @Preview
              @ComposePreviewLabOption(collectScopes = ["acme_ui"])
              fun MyButtonPreview() { ... }

              val previews by collectModulePreviews(scope = "acme_ui")
            """.trimIndent()

        /**
         * Reply for `NonLiteralScopeIrError` — IR-side mirror of the FIR
         * `NON_LITERAL_COLLECT_SCOPE` diagnostic.
         *
         * Includes the `@ComposePreviewLabOption(collectScopes = [...])` annotation form so
         * users see the canonical way to surface a non-default scope.
         */
        val LiteralScopeOnly: String =
            """
            Pass a compile-time string constant for `scope = ...` — either an inline string
            literal (`scope = "acme_ui"`) or a `const val` reference. Non-`const` vals,
            string concatenations, and function calls cannot be embedded into the synthetic
            identifier and are rejected. Tag the matching @Preview functions on the producer
            side with @ComposePreviewLabOption so they participate in the scope:

              @Preview
              @ComposePreviewLabOption(collectScopes = ["acme_ui"])
              fun MyButtonPreview() { ... }

              val previews by collectModulePreviews(scope = "acme_ui")
            """.trimIndent()
    }
}
