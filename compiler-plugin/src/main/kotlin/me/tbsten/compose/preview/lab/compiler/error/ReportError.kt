package me.tbsten.compose.preview.lab.compiler.error

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

/**
 * Reports [error] through this [MessageCollector] as a `CompilerMessageSeverity.ERROR`,
 * including the rendered "Context:" / "How to reply:" sections built from the
 * structured fields on [ComposePreviewLabCompilerPluginError].
 *
 * Prefer this over `messageCollector.report(ERROR, "...", location)` with a literal
 * string — see `.claude/rules/compiler-plugin-error.md`.
 *
 * **Sample call**:
 * ```kotlin
 * messageCollector.report(
 *     HintHashCollisionError(hash, previewA, previewB),
 *     declarationLocation(property),
 * )
 * ```
 *
 * **Rendered output (one ERROR diagnostic)**:
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
 *     This is an unexpected internal error. Please report it at ...
 * ```
 *
 * `location` is forwarded as-is — passing `null` is supported (renderer never NPEs on
 * null locations) for synthetic sites that do not have an associated `IrFile`.
 */
fun MessageCollector.report(error: ComposePreviewLabCompilerPluginError, location: CompilerMessageLocation?) {
    report(
        CompilerMessageSeverity.ERROR,
        buildErrorBody(error),
        location,
    )
}

/**
 * Composes the rendered body of [error] as a single multi-line `String`.
 *
 * Body shape:
 * ```
 * [ComposePreviewLab/<categories>] <message>
 *
 *     <description line 1>
 *     <description line 2>
 *
 *   Context:
 *     <label>: <value>
 *     ...
 *
 *   How to reply:
 *     <reply line 1>
 *     <reply line 2>
 *     ...
 * ```
 *
 * Each optional section (description / context / replies) is omitted entirely (along
 * with its preceding blank line) when the underlying property is empty. The trailing
 * `\n` produced by `appendLine` is trimmed so the final character is the last visible
 * one — without this trim some Gradle renderers print a stray blank line that makes
 * stack-trace context harder to read.
 */
internal fun buildErrorBody(error: ComposePreviewLabCompilerPluginError): String = buildString {
    val tag = "[ComposePreviewLab/${error.categories.joinToString(",") { it.name }}]"
    appendLine("$tag ${error.message}")

    val description = error.description
    if (!description.isNullOrEmpty()) {
        appendLine()
        description.lineSequence().forEach { appendLine("    $it") }
    }

    if (error.context.isNotEmpty()) {
        appendLine()
        appendLine("  Context:")
        error.context.forEach { entry ->
            appendLine("    $entry")
        }
    }

    if (error.replies.isNotEmpty()) {
        appendLine()
        appendLine("  How to reply:")
        error.replies.forEach { reply ->
            reply.lineSequence().forEach { appendLine("    $it") }
        }
    }
}.trimEnd('\n')

/**
 * Exception that wraps a structured [ComposePreviewLabCompilerPluginError] for the
 * defensive `error("...")` ports.
 *
 * The message is built once via [buildErrorBody] so that the eventual stack-trace log
 * line reads identically to the corresponding `MessageCollector.report(...)` output.
 * `IllegalStateException` is the underlying type so callers that catch the existing
 * `error("...")` `IllegalStateException` keep working unchanged.
 */
class ComposePreviewLabCompilerPluginException(val error: ComposePreviewLabCompilerPluginError, cause: Throwable? = null) :
    IllegalStateException(buildErrorBody(error), cause)

/**
 * Throws [this] error as a [ComposePreviewLabCompilerPluginException]. Replacement for
 * raw `error("...")` calls in defensive `?:` chains (see `Errors.kt`:
 * `PropertyHasNoGetterError`, `PreviewExportNotFoundError`,
 * `RuntimeFunctionNotFoundError`).
 *
 * **Sample call**:
 * ```kotlin
 * val getter = property.getter ?: PropertyHasNoGetterError(callableId).throwAsException()
 * ```
 *
 * Return type is `Nothing` so the call composes with the `?: ...` chain without an
 * extra `return @run` shim.
 */
fun ComposePreviewLabCompilerPluginError.throwAsException(cause: Throwable? = null): Nothing =
    throw ComposePreviewLabCompilerPluginException(error = this, cause = cause)
