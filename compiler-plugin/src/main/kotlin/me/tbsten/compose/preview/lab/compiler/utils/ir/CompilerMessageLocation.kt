@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.utils.ir

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.file

/**
 * Resolves the source location of an [IrDeclaration] for use in `MessageCollector` reports.
 *
 * Accepts any `IrDeclaration` so the helper can be reused across transformers — properties
 * carrying `collect[All]ModulePreviews(...)` delegates, hint stubs being filled, etc. The
 * return value is suitable for the `location` argument of
 * `MessageCollector.report(severity, message, location)` (or the structured
 * `MessageCollector.report(error, location)` extension defined under `error/`).
 *
 * **Sample call → returned location**:
 * ```kotlin
 * // Source: src/main/kotlin/com/example/Previews.kt
 * //   12 | val myPreviews by collectModulePreviews()
 * //
 * // myPreviewsProperty.compilerMessageLocation()
 * //   → CompilerMessageLocation(path="src/main/kotlin/com/example/Previews.kt", line=12, column=5, lineContent=null)
 * ```
 *
 * Returns `null` when the declaration has no associated `IrFile` (synthetic IR built from
 * scratch) or when its `startOffset` is `UNDEFINED_OFFSET` (`-1`). Callers that need a
 * fallback should handle the null themselves — this helper deliberately avoids fabricating
 * a location.
 */
internal fun IrDeclaration.compilerMessageLocation(): CompilerMessageLocation? {
    val entry = runCatching { file }.getOrNull()?.fileEntry ?: return null
    val offset = startOffset.takeIf { it >= 0 } ?: return null
    val line = entry.getLineNumber(offset) + 1
    val column = entry.getColumnNumber(offset) + 1
    return CompilerMessageLocation.create(entry.name, line, column, null)
}

/**
 * Convenience overload that resolves the source location of an [IrSimpleFunction] — a
 * common case (every hint stub / `@Preview` body filler reports at its function header).
 * Delegates to the [IrDeclaration] receiver overload so both return shape and null
 * semantics stay identical.
 *
 * **Sample call → returned location**:
 * ```kotlin
 * // Source: src/main/kotlin/com/example/MyButton.kt
 * //   10 | @Preview fun MyButton() { ... }
 * //
 * // myButtonFunction.compilerMessageLocation()
 * //   → CompilerMessageLocation(path="src/main/kotlin/com/example/MyButton.kt", line=10, column=1, lineContent=null)
 * ```
 */
internal fun IrSimpleFunction.compilerMessageLocation(): CompilerMessageLocation? =
    (this as IrDeclaration).compilerMessageLocation()
