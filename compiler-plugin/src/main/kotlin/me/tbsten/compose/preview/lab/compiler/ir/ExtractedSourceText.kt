@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.ir

import java.io.File
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.file

/**
 * Result of source text extraction from an IR function.
 *
 * @property code The function body source text, or null if unavailable.
 * @property kdoc The KDoc comment preceding the function, or null if none found.
 */
internal data class ExtractedSourceText(val code: String?, val kdoc: String?)

/** Cache of file contents keyed by file path to avoid re-reading the same file for every @Preview function. */
private val fileContentCache = mutableMapOf<String, String>()

/**
 * Extracts the source code body and KDoc comment from an [IrSimpleFunction].
 *
 * Reads the source file from disk using the function's [IrFileEntry] path.
 * File contents are cached by path so that repeated calls for functions in the
 * same file do not re-read from disk.
 * Returns nulls if the file does not exist (e.g. in-memory virtual files during testing).
 *
 * @param func The IR function to extract source text from.
 * @return The extracted source code and KDoc.
 */
internal fun extractSourceText(func: IrSimpleFunction): ExtractedSourceText {
    val fileEntry = func.file.fileEntry
    val filePath = fileEntry.name
    val file = File(filePath)
    if (!file.exists()) return ExtractedSourceText(null, null)

    val content = fileContentCache.getOrPut(filePath) { file.readText() }

    val code = extractCode(func, content)
    val kdoc = extractKDoc(func.startOffset, content)

    return ExtractedSourceText(code, kdoc)
}

private fun extractCode(func: IrSimpleFunction, content: String): String? {
    val body = func.body ?: return null
    val start = body.startOffset
    val end = body.endOffset
    if (start < 0 || end > content.length || start >= end) return null
    return content.substring(start, end)
}

/**
 * Extracts a `/** ... */` KDoc block immediately before the function start offset,
 * skipping any annotation lines between the KDoc and the function declaration.
 */
private fun extractKDoc(funcStart: Int, content: String): String? {
    if (funcStart <= 0 || funcStart > content.length) return null
    val beforeFunc = content.substring(0, funcStart)

    val regex = Regex("""/\*\*(.*?)\*/\s*(?:\s*@[^\n]*\n)*\s*$""", RegexOption.DOT_MATCHES_ALL)
    val match = regex.find(beforeFunc) ?: return null

    return match.groupValues[1]
        .lines()
        .map { it.trim().removePrefix("*").trim() }
        .filter { it.isNotBlank() }
        .joinToString("\n")
        .takeIf { it.isNotEmpty() }
}
