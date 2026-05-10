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
