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
    /**
     * Collection scopes from `@ComposePreviewLabOption(collectScope = [...])`, defaulting to
     * `["default"]` when no annotation or scope override is present. The IR pass uses this to
     * filter the same-module preview list per `collect[All]ModulePreviews(scope = ...)`
     * call: a preview ends up in the result whenever the requested scope appears anywhere
     * in this list (so `collectScope = ["design", "showcase"]` makes the same preview show
     * up in both galleries).
     */
    val scopes: List<String>,
)
