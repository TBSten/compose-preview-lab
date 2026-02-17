package me.tbsten.compose.preview.lab.compiler.ir

import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

internal data class PreviewMetadata(
    val irFunction: IrSimpleFunction,
    val packageName: String,
    val simpleName: String,
    val qualifiedName: String,
    val displayName: String,
    val id: String,
    val filePath: String?,
    val startLineNumber: Int?,
    val code: String?,
    val kdoc: String?,
)
