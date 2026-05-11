package me.tbsten.compose.preview.lab.compiler.error

import me.tbsten.compose.preview.lab.compiler.error.ComposePreviewLabCompilerPluginError.Category
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

/**
 * Reports multiple [errors] as a single `CompilerMessageSeverity.ERROR` diagnostic, in the
 * spirit of Kotest's `assertSoftly { ... }`. Useful when several distinct invariants fail
 * in one code path and surfacing them all at once gives a better signal than reporting
 * only the first one.
 *
 * Rendered output:
 * ```
 * [ComposePreviewLab/IR,FIR,...] 複数のエラーが発生しました:
 *   - <error1.message>
 *   - <error2.message>
 *   - <error3.message>
 *
 *   Errors:
 *
 *     <buildErrorBody(error1) — indented>
 *
 *     <buildErrorBody(error2) — indented>
 *
 *     <buildErrorBody(error3) — indented>
 * ```
 *
 * Behaviour for the boundary cases:
 * - `errors` empty: returns without reporting (matches `Iterable<*>` no-op semantics on
 *   `joinToString` etc.).
 * - `errors` size 1: delegates to the single-error overload `report(error, location)` so
 *   the output is identical to the non-batched call (no `複数のエラーが発生しました:` wrapper).
 */
fun MessageCollector.report(vararg errors: ComposePreviewLabCompilerPluginError, location: CompilerMessageLocation?) {
    when (errors.size) {
        0 -> return
        1 -> report(errors[0], location)
        else -> report(CompilerMessageSeverity.ERROR, buildAggregatedErrorBody(errors.toList()), location)
    }
}

/**
 * Throws a single [ComposePreviewLabCompilerPluginException] that wraps all [errors] —
 * the primary `error` is a synthetic [AggregateError] holding all summaries, and each
 * individual error is attached via `addSuppressed` so the resulting stack trace lists
 * every collected error after the "suppressed by:" header.
 *
 * Designed to be the `throwAsException` counterpart of `report(vararg errors, location)`
 * for the defensive paths where reporting via `MessageCollector` is not available
 * (`init` blocks, lazy property initializers, ...).
 *
 * **Behaviour for the boundary cases**:
 * - `errors` empty: this function is `Nothing`, but with no errors there is nothing to
 *   throw — we throw an [IllegalArgumentException] so the caller bug is loud.
 * - `errors` size 1: delegates to the single-error `throwAsException()` overload so the
 *   message and stack trace are identical to the non-batched call (no synthetic aggregate
 *   wrapper).
 */
fun throwAsException(vararg errors: ComposePreviewLabCompilerPluginError, cause: Throwable? = null): Nothing {
    when (errors.size) {
        0 -> throw IllegalArgumentException(
            "throwAsException(vararg) requires at least one error — got zero. " +
                "This is a programmer bug in the call site.",
        )
        1 -> errors[0].throwAsException(cause)
        else -> {
            val aggregate = AggregateError(errors.toList())
            val ex = ComposePreviewLabCompilerPluginException(aggregate, cause = cause)
            errors.forEach { ex.addSuppressed(ComposePreviewLabCompilerPluginException(it)) }
            throw ex
        }
    }
}

/**
 * Renders the aggregated error body shared by [report] (vararg overload) and
 * [throwAsException] (vararg overload).
 *
 * Aggregated categories are the **union** of all child categories in encounter order, so
 * the prefix conveys the breadth of the failure (e.g. `[ComposePreviewLab/IR,FIR]` if a
 * mix of IR + FIR errors lands in one batch).
 */
internal fun buildAggregatedErrorBody(errors: List<ComposePreviewLabCompilerPluginError>): String = buildString {
    val unionCategories = errors.flatMap { it.categories }.distinct()
    val tag = "[ComposePreviewLab/${unionCategories.joinToString(",") { it.name }}]"
    appendLine("$tag 複数のエラーが発生しました:")
    errors.forEach { appendLine("  - ${it.message}") }
    appendLine()
    appendLine("  Errors:")
    errors.forEach { error ->
        appendLine()
        buildErrorBody(error).lineSequence().forEach { appendLine("    $it") }
    }
}.trimEnd('\n')

/**
 * Synthetic aggregate [ComposePreviewLabCompilerPluginError] that wraps multiple
 * sub-errors. Used as the primary `error` of a `ComposePreviewLabCompilerPluginException`
 * when [throwAsException] is called with `vararg` of size > 1, so callers that pattern
 * match on `ComposePreviewLabCompilerPluginException.error` can detect the aggregate
 * case and reach the suppressed exceptions for individual errors.
 */
internal class AggregateError(val errors: List<ComposePreviewLabCompilerPluginError>) : ComposePreviewLabCompilerPluginError {
    override val categories: List<Category> = errors.flatMap { it.categories }.distinct()
    override val message: String = "複数のエラーが発生しました: ${errors.joinToString(", ") { it.message }}"
    override val description: String? = null
    override val context: List<String> = emptyList()
    override val replies: List<String> = errors.flatMap { it.replies }.distinct()
}
