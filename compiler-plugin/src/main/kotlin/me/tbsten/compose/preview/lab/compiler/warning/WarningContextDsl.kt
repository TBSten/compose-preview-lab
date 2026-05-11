package me.tbsten.compose.preview.lab.compiler.warning

/**
 * `@DslMarker` for the [WarningContextBuilder] DSL.
 *
 * Per the Kotlin DSL marker rules ([scope control](https://kotlinlang.org/docs/type-safe-builders.html#scope-control-dslmarker)),
 * only receivers tagged with the **same** `@DslMarker` annotation isolate each other,
 * so this marker isolates nested `WarningContextBuilder` receivers from one another —
 * not from `ErrorContextBuilder`. Keeping the warning marker separate from
 * `@ErrorContextDsl` lets each builder evolve its own DSL surface (extension functions /
 * operators tagged with one marker do not bleed into the other receiver's scope).
 */
@DslMarker
annotation class WarningContextDsl

/**
 * Mutable builder used by [contextOf] to assemble the `List<String>` for a
 * [ComposePreviewLabCompilerPluginWarning] implementation. Mirrors `ErrorContextBuilder`
 * — see the error-side KDoc for the design rationale.
 *
 * **Sample call**:
 * ```kotlin
 * override val context = contextOf {
 *     "hint_marker"(markerFqn)
 *     "scope"(scope)
 *     "isVersionGated"()
 * }
 * // → listOf("hint_marker: <markerFqn>", "scope: <scope>", "isVersionGated")
 * ```
 */
@WarningContextDsl
class WarningContextBuilder internal constructor() {
    private val entries = mutableListOf<String>()

    /** `"label"(value)` → appends `"<label>: <value>"`. */
    operator fun String.invoke(value: String) {
        entries.add("$this: $value")
    }

    /** `"isHoge"()` → appends the receiver verbatim (no-arg overload for boolean / tag entries). */
    operator fun String.invoke() {
        entries.add(this)
    }

    /** Snapshot of the accumulated entries in insertion order. */
    internal fun build(): List<String> = entries.toList()
}

/**
 * Builds a `List<String>` using the [WarningContextBuilder] DSL.
 *
 * Restricted to `ComposePreviewLabCompilerPluginWarning` receivers (parallel to the
 * error-side `ComposePreviewLabCompilerPluginError.contextOf`) so the DSL cannot be
 * called outside a warning implementation's `override val context = ...` site.
 */
@Suppress("UnusedReceiverParameter")
fun ComposePreviewLabCompilerPluginWarning.contextOf(block: WarningContextBuilder.() -> Unit): List<String> =
    WarningContextBuilder().apply(block).build()
