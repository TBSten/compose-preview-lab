package me.tbsten.compose.preview.lab.compiler.error

/**
 * `@DslMarker` for the [ErrorContextBuilder] DSL.
 *
 * Per the Kotlin DSL marker rules ([scope control](https://kotlinlang.org/docs/type-safe-builders.html#scope-control-dslmarker)),
 * only receivers tagged with the **same** `@DslMarker` annotation isolate each other.
 * The point of giving `ErrorContextBuilder` its own marker (rather than reusing a
 * "ContextDsl" marker shared with `WarningContextBuilder`) is to keep the two builders
 * independently extensible — future `Error`-specific DSL helpers can be added under
 * `@ErrorContextDsl` without changing how warning receivers behave, and vice versa.
 */
@DslMarker
annotation class ErrorContextDsl

/**
 * Mutable builder used by [contextOf] to assemble the `List<String>` for an
 * [ComposePreviewLabCompilerPluginError] implementation.
 *
 * The accumulated list contains one rendered "Context:" line per entry. Two shorthand
 * forms are supported, both spelled as a `String` invocation so no `+` prefix is needed:
 *
 * - `"label"(value)` — `label: value` style. The receiver `String` is the label, and
 *   the operator invocation appends `"<label>: <value>"`.
 * - `"isHoge"()` — single-token boolean / tag style. Use the no-argument
 *   `String.invoke()` overload to append the receiver verbatim, sidestepping the noisy
 *   `"isHoge: true"` shape for boolean flag entries.
 *
 * **Sample call**:
 * ```kotlin
 * val entries = ErrorContextBuilder().apply {
 *     "hash"("abc1234")
 *     "preview_a"("com.example.A(Int)")
 *     "isHoge"()
 * }.build()
 * // entries == listOf("hash: abc1234", "preview_a: com.example.A(Int)", "isHoge")
 * ```
 *
 * Insertion order is preserved and duplicate labels are not deduplicated.
 */
@ErrorContextDsl
class ErrorContextBuilder internal constructor() {
    private val entries = mutableListOf<String>()

    /**
     * Appends `<this>: <value>` to the accumulating list. Designed to be called as a
     * top-level expression inside the [contextOf] block:
     *
     * ```kotlin
     * override val context = contextOf {
     *     "hash"(hash)        // → "hash: <hash>"
     *     "preview_a"(previewA)
     * }
     * ```
     */
    operator fun String.invoke(value: String) {
        entries.add("$this: $value")
    }

    /**
     * Appends the receiver `String` verbatim to the accumulating list. Use this no-arg
     * overload for boolean / tag entries where the noisy `"isHoge: true"` shape is
     * undesired:
     *
     * ```kotlin
     * override val context = contextOf {
     *     "isVersionGated"()
     *     "call"(callName)
     * }
     * // → listOf("isVersionGated", "call: <callName>")
     * ```
     */
    operator fun String.invoke() {
        entries.add(this)
    }

    /** Snapshot of the accumulated entries in insertion order. */
    internal fun build(): List<String> = entries.toList()
}

/**
 * Builds a `List<String>` using the [ErrorContextBuilder] DSL.
 *
 * Restricted to `ComposePreviewLabCompilerPluginError` receivers so that the DSL cannot
 * leak into unrelated `override val context` definitions (e.g. on the parallel Warning
 * framework, which has its own `WarningContextBuilder`). The receiver is unused at
 * runtime — only its compile-time type is needed for the call-site type check.
 *
 * **Sample call**:
 * ```kotlin
 * class HintHashCollisionError(...) : ComposePreviewLabCompilerPluginError {
 *     override val context = contextOf {
 *         "hash"(hash)
 *         "preview_a"(previewA)
 *         "preview_b"(previewB)
 *     }
 * }
 * ```
 */
@Suppress("UnusedReceiverParameter")
fun ComposePreviewLabCompilerPluginError.contextOf(block: ErrorContextBuilder.() -> Unit): List<String> =
    ErrorContextBuilder().apply(block).build()
