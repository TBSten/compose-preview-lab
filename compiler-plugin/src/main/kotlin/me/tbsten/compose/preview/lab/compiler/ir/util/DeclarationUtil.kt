@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.ir.util

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.file

/**
 * Resolves the source location of [declaration] for use in `MessageCollector` reports.
 *
 * Accepts any `IrDeclaration` so the helper can be reused across transformers — properties
 * carrying `collect[All]ModulePreviews(...)` delegates, hint stubs being filled, etc. The
 * return value is suitable for the `location` argument of `MessageCollector.report(...)`.
 *
 * **Sample call → returned location**:
 * ```kotlin
 * // Source: src/main/kotlin/com/example/Previews.kt
 * //   12 | val myPreviews by collectModulePreviews()
 * //
 * // declarationLocation(myPreviewsProperty)
 * //   → CompilerMessageLocation(path="src/main/kotlin/com/example/Previews.kt", line=12, column=5, lineContent=null)
 * ```
 *
 * Returns `null` when the declaration has no associated `IrFile` (synthetic IR built from
 * scratch) or when its `startOffset` is `UNDEFINED_OFFSET` (`-1`). Callers that need a
 * fallback should handle the null themselves — this helper deliberately avoids fabricating
 * a location.
 */
internal fun declarationLocation(declaration: IrDeclaration): CompilerMessageLocation? {
    val entry = runCatching { declaration.file }.getOrNull()?.fileEntry ?: return null
    val offset = declaration.startOffset.takeIf { it >= 0 } ?: return null
    val line = entry.getLineNumber(offset) + 1
    val column = entry.getColumnNumber(offset) + 1
    return CompilerMessageLocation.create(entry.name, line, column, null)
}
