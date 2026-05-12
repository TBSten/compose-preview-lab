@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package me.tbsten.compose.preview.lab

/**
 * Type-safe binding for the browser's `console.warn(message)` from Wasm/JS.
 *
 * Using `@JsFun` over a raw `js("console.warn(message)")` block makes the parameter
 * marshalling go through Kotlin's signature, so the compiler guarantees the JS
 * argument named in the snippet binds to the Kotlin `message` parameter — independent
 * of whatever local-name mangling the Wasm/JS lowering applies. The naive
 * `js("console.warn(message)")` form references `message` as a literal JS identifier
 * and breaks at runtime in builds that mangle/rename locals.
 */
@JsFun("(message) => console.warn(message)")
private external fun consoleWarn(message: String)

@InternalComposePreviewLabApi
public actual fun warnDuplicatePreview(message: String) {
    // The shared [WarnPrefix] is prepended in Kotlin (rather than baked into the
    // `@JsFun` body) so the prefix stays the single source of truth in `commonMain`
    // and so the Kotlin-level concatenation goes through normal string handling
    // rather than the JS-side parameter marshalling.
    consoleWarn("$WarnPrefix$message")
}
