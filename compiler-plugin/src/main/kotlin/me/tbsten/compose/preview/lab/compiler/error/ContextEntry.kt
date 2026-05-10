package me.tbsten.compose.preview.lab.compiler.error

/**
 * One `label: value` line inside the "Context:" section of a rendered error / warning.
 *
 * Defined at top-level (not nested inside [ComposePreviewLabCompilerPluginError]) so that
 * both `Error` and `Warning` interfaces can share the same data type without a
 * `typealias`-with-double-definition split. Plain top-level keeps the import path
 * unambiguous and avoids a Single-Source-of-Truth violation if a future Warning attempted
 * to define its own [ContextEntry].
 *
 * @property label Human-readable tag printed as the key (e.g. `"hash"`, `"call"`,
 *  `"current_kotlin_version"`). Free-form string; no validation.
 * @property value String representation of the dynamic value. Callers are responsible
 *  for `toString()` themselves (the type is `String` rather than `Any` on purpose so the
 *  renderer formatting stays consistent).
 */
data class ContextEntry(val label: String, val value: String)
