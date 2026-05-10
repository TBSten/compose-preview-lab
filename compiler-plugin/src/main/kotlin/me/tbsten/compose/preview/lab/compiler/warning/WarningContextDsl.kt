package me.tbsten.compose.preview.lab.compiler.warning

import me.tbsten.compose.preview.lab.compiler.error.ContextEntry

/**
 * `@DslMarker` for the [WarningContextBuilder] DSL. Separate from the error-side
 * `@ErrorContextDsl` annotation so nested receiver scopes (e.g. a warning being built
 * inside an `if`-guard on an error code path) cannot accidentally cross over.
 */
@DslMarker
annotation class WarningContextDsl

/**
 * Mutable builder used by [contextOf] to assemble the `List<ContextEntry>` for a
 * [ComposePreviewLabCompilerPluginWarning] implementation. Identical in shape to
 * `ErrorContextBuilder` — see the error-side KDoc for the design rationale.
 *
 * **Sample call**:
 * ```kotlin
 * override val context = contextOf {
 *     +"hint_marker"(markerFqn)
 *     +"scope"(scope)
 * }
 * ```
 */
@WarningContextDsl
class WarningContextBuilder internal constructor() {
    private val entries = mutableListOf<ContextEntry>()

    /** Plain function alternative to `+"label"(value)` for readability-sensitive sites. */
    fun entry(label: String, value: String): ContextEntry = ContextEntry(label = label, value = value)

    /** `"label"(value)` → `ContextEntry(label="label", value=value)`. */
    operator fun String.invoke(value: String): ContextEntry = ContextEntry(label = this, value = value)

    /** Appends [this] entry to the builder's accumulating list. */
    operator fun ContextEntry.unaryPlus() {
        entries.add(this)
    }

    /** Snapshot of the accumulated entries in insertion order. */
    internal fun build(): List<ContextEntry> = entries.toList()
}

/**
 * Builds a `List<ContextEntry>` using the [WarningContextBuilder] DSL.
 *
 * Restricted to `ComposePreviewLabCompilerPluginWarning` receivers (parallel to the
 * error-side `ComposePreviewLabCompilerPluginError.contextOf`) so the DSL cannot be
 * called outside a warning implementation's `override val context = ...` site.
 */
@Suppress("UnusedReceiverParameter")
fun ComposePreviewLabCompilerPluginWarning.contextOf(block: WarningContextBuilder.() -> Unit): List<ContextEntry> =
    WarningContextBuilder().apply(block).build()
