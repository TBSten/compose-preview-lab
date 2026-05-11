package me.tbsten.compose.preview.lab.compiler.warning

import me.tbsten.compose.preview.lab.compiler.error.ComposePreviewLabCompilerPluginError.Category
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

/**
 * Reports multiple [warnings] as a single `CompilerMessageSeverity.WARNING` diagnostic.
 * Symmetric to the error-side `report(vararg errors, location)` — see that function for
 * the rationale (Kotest-`assertSoftly` analogue) and rendered shape.
 *
 * Behaviour for the boundary cases:
 * - `warnings` empty: returns without reporting.
 * - `warnings` size 1: delegates to the single-warning overload.
 */
fun MessageCollector.report(vararg warnings: ComposePreviewLabCompilerPluginWarning, location: CompilerMessageLocation?) {
    when (warnings.size) {
        0 -> return
        1 -> report(warnings[0], location)
        else -> report(CompilerMessageSeverity.WARNING, buildAggregatedWarningBody(warnings.toList()), location)
    }
}

/**
 * Renders the aggregated warning body. Aggregated categories are the union of all child
 * categories in encounter order (mirroring `buildAggregatedErrorBody`).
 */
internal fun buildAggregatedWarningBody(warnings: List<ComposePreviewLabCompilerPluginWarning>): String = buildString {
    val unionCategories: List<Category> = warnings.flatMap { it.categories }.distinct()
    val tag = "[ComposePreviewLab/${unionCategories.joinToString(",") { it.name }}]"
    appendLine("$tag 複数の警告が発生しました:")
    warnings.forEach { appendLine("  - ${it.message}") }
    appendLine()
    appendLine("  Warnings:")
    warnings.forEach { warning ->
        appendLine()
        buildWarningBody(warning).lineSequence().forEach { appendLine("    $it") }
    }
}.trimEnd('\n')
