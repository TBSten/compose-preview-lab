package me.tbsten.compose.preview.lab.compiler.ir

import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

/**
 * @Preview アノテーション付き関数から収集したメタデータ。
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
