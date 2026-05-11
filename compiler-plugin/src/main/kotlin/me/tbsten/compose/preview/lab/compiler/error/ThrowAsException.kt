package me.tbsten.compose.preview.lab.compiler.error

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
 * extra `return @run` shim. Prefer [orThrow] for the cleaner `hoge.orThrow { SomeError() }`
 * form on a `T?` receiver — `throwAsException()` is the lower-level building block when
 * the caller already has a constructed error and just needs to throw it.
 */
fun ComposePreviewLabCompilerPluginError.throwAsException(cause: Throwable? = null): Nothing =
    throw ComposePreviewLabCompilerPluginException(error = this, cause = cause)

/**
 * Nullable-receiver helper: returns the receiver when non-null, otherwise throws
 * [errorBuilder]`()` via [throwAsException].
 *
 * Designed to replace the verbose
 * ```kotlin
 * val hoge: Hoge = someNullable() ?: SomeError(args).throwAsException()
 * ```
 * idiom with the more readable
 * ```kotlin
 * val hoge: Hoge = someNullable().orThrow { SomeError(args) }
 * ```
 *
 * The error is built lazily so [errorBuilder] only runs on the throwing path — useful
 * when the error construction itself allocates (FQN strings, callable id chains, ...).
 */
inline fun <T : Any> T?.orThrow(errorBuilder: () -> ComposePreviewLabCompilerPluginError): T =
    this ?: errorBuilder().throwAsException()
