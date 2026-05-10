package me.tbsten.compose.preview.lab.compiler.error

/**
 * `@DslMarker` for the [ErrorContextBuilder] DSL.
 *
 * Prevents implicit calls from outer receiver scopes that also happen to be tagged with
 * a different `@DslMarker`, so a future `WarningContextBuilder` invocation inside a
 * `contextOf { ... }` block won't be silently resolved against the wrong builder.
 */
@DslMarker
annotation class ErrorContextDsl

/**
 * Mutable builder used by [contextOf] to assemble the `List<ContextEntry>` for an
 * [ComposePreviewLabCompilerPluginError] implementation.
 *
 * **Sample call**:
 * ```kotlin
 * val entries = ErrorContextBuilder().apply {
 *     +"hash"("abc1234")
 *     +"preview_a"("com.example.A(Int)")
 * }.build()
 * // entries == listOf(ContextEntry("hash", "abc1234"), ContextEntry("preview_a", "com.example.A(Int)"))
 * ```
 *
 * Same `label` may be appended multiple times — the builder preserves insertion order
 * and does not deduplicate. The two-step `+"label"(value)` syntax is the recommended
 * shorthand; the plain alternative [entry] is provided for cases where the operator
 * chain is harder to read (e.g. `if`-conditional entries inside a higher-order helper).
 */
@ErrorContextDsl
class ErrorContextBuilder internal constructor() {
    private val entries = mutableListOf<ContextEntry>()

    /**
     * Plain function alternative to `+"label"(value)` for callers that prefer not to
     * read the two-step operator chain.
     *
     * ```kotlin
     * override val context = contextOf {
     *     +entry("hash", hash)
     *     +entry("preview_a", previewA)
     * }
     * ```
     *
     * Returns a [ContextEntry] without appending it to the builder; combine with
     * [unaryPlus] to add it to the resulting list.
     */
    fun entry(label: String, value: String): ContextEntry = ContextEntry(label = label, value = value)

    /**
     * Treats a `String` receiver as a label and creates a [ContextEntry] paired with
     * [value]. Designed to compose with [unaryPlus] for the
     * `+"label"(value)` shorthand:
     *
     * ```kotlin
     * override val context = contextOf {
     *     +"hash"(hash)        // == +entry("hash", hash)
     * }
     * ```
     */
    operator fun String.invoke(value: String): ContextEntry = ContextEntry(label = this, value = value)

    /**
     * Appends [this] entry to the builder's accumulating list. Returns `Unit` because
     * the entry has already been recorded — chaining is intentionally unsupported.
     */
    operator fun ContextEntry.unaryPlus() {
        entries.add(this)
    }

    /** Snapshot of the accumulated entries in insertion order. */
    internal fun build(): List<ContextEntry> = entries.toList()
}

/**
 * Builds a `List<ContextEntry>` using the [ErrorContextBuilder] DSL.
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
 *         +"hash"(hash)
 *         +"preview_a"(previewA)
 *         +"preview_b"(previewB)
 *     }
 * }
 * ```
 */
@Suppress("UnusedReceiverParameter")
fun ComposePreviewLabCompilerPluginError.contextOf(block: ErrorContextBuilder.() -> Unit): List<ContextEntry> =
    ErrorContextBuilder().apply(block).build()
