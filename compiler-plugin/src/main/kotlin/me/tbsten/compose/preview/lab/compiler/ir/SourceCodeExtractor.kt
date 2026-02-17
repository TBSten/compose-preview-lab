package me.tbsten.compose.preview.lab.compiler.ir

import java.io.File
import org.jetbrains.kotlin.ir.IrFileEntry
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

internal object SourceCodeExtractor {
    fun extractFunctionBody(fileEntry: IrFileEntry, function: IrSimpleFunction): String? =
        try {
            val sourceFile = File(fileEntry.name)
            if (!sourceFile.exists()) return null

            val sourceText = sourceFile.readText()
            val startOffset = function.startOffset
            val endOffset = function.endOffset
            if (startOffset < 0 || endOffset < 0 || endOffset > sourceText.length) return null

            val functionText = sourceText.substring(startOffset, endOffset)

            // Extract body: find the first '{' and extract content between braces
            val bodyStart = functionText.indexOf('{')
            if (bodyStart < 0) {
                // Expression body: find '=' and take the rest
                val eqIndex = functionText.indexOf('=')
                if (eqIndex >= 0) {
                    functionText.substring(eqIndex + 1).trim()
                } else {
                    null
                }
            } else {
                val bodyEnd = functionText.lastIndexOf('}')
                if (bodyEnd > bodyStart) {
                    functionText.substring(bodyStart + 1, bodyEnd).trimIndent().trim()
                } else {
                    null
                }
            }
        } catch (_: Exception) {
            null
        }

    fun extractKDoc(fileEntry: IrFileEntry, function: IrSimpleFunction): String? =
        try {
            val sourceFile = File(fileEntry.name)
            if (!sourceFile.exists()) return null

            val sourceText = sourceFile.readText()
            val startOffset = function.startOffset
            if (startOffset < 0) return null

            // Search backwards from the function start for KDoc
            val textBefore = sourceText.substring(0, startOffset).trimEnd()
            if (textBefore.endsWith("*/")) {
                val kdocStart = textBefore.lastIndexOf("/**")
                if (kdocStart >= 0) {
                    textBefore.substring(kdocStart)
                        .removePrefix("/**")
                        .removeSuffix("*/")
                        .lines()
                        .joinToString("\n") { it.trim().removePrefix("*").trim() }
                        .trim()
                        .ifEmpty { null }
                } else {
                    null
                }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
}
