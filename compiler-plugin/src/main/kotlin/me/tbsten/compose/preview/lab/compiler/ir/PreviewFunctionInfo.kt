package me.tbsten.compose.preview.lab.compiler.ir

import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

/**
 * Metadata collected from an `@Preview`-annotated function.
 */
internal data class PreviewFunctionInfo(
    val function: IrSimpleFunction,
    val id: String,
    val displayName: String,
    val filePath: String?,
    val startLineNumber: Int?,
    val endLineNumber: Int?,
    val code: String?,
    val kdoc: String?,
)
