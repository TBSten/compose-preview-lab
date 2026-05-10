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
 * surfaces them through [context] using the `contextOf { +"label"(value) }` DSL so the
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
     * Ordered list of `(label, value)` pairs that describe the dynamic context for the
     * specific failure (FQN involved, hash that collided, version that was detected, ...).
     * Renderer prints each as `<label>: <value>` under a "Context:" header.
     *
     * Same label may appear multiple times (e.g. two `"preview"` entries for the two
     * collided previews). [ContextEntry] is a top-level data class in this package so
     * the same type can be shared with the parallel `warning/` framework without a
     * `typealias` re-export.
     */
    val context: List<ContextEntry> get() = emptyList()

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
}
