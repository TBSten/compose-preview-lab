package me.tbsten.compose.preview.lab.compiler.warning

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

/**
 * Reports [warning] through this [MessageCollector] as a
 * `CompilerMessageSeverity.WARNING`, including the rendered "Context:" / "How to
 * reply:" sections built from the structured fields on
 * [ComposePreviewLabCompilerPluginWarning].
 *
 * Mirrors the error-side `MessageCollector.report(error, location)` extension so the
 * two reporters share an identical body shape — only the severity and the
 * `Warning` / `Error` interface differ.
 */
fun MessageCollector.report(warning: ComposePreviewLabCompilerPluginWarning, location: CompilerMessageLocation?) {
    report(
        CompilerMessageSeverity.WARNING,
        buildWarningBody(warning),
        location,
    )
}

/**
 * Composes the rendered body of [warning] as a single multi-line `String`.
 *
 * Mirrors `buildErrorBody` on the error side; sections (description / context /
 * replies) are omitted along with their preceding blank line when the underlying
 * field is empty. Trailing `\n` is trimmed so Gradle's renderer does not insert a
 * stray blank line below the diagnostic.
 */
internal fun buildWarningBody(warning: ComposePreviewLabCompilerPluginWarning): String = buildString {
    val tag = "[ComposePreviewLab/${warning.categories.joinToString(",") { it.name }}]"
    appendLine("$tag ${warning.message}")

    val description = warning.description
    if (!description.isNullOrEmpty()) {
        appendLine()
        description.lineSequence().forEach { appendLine("    $it") }
    }

    if (warning.context.isNotEmpty()) {
        appendLine()
        appendLine("  Context:")
        warning.context.forEach { entry ->
            appendLine("    $entry")
        }
    }

    if (warning.replies.isNotEmpty()) {
        appendLine()
        appendLine("  How to reply:")
        warning.replies.forEach { reply ->
            reply.lineSequence().forEach { appendLine("    $it") }
        }
    }
}.trimEnd('\n')
