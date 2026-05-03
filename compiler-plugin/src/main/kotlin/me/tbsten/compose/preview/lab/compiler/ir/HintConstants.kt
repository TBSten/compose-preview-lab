package me.tbsten.compose.preview.lab.compiler.ir

import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * Shared constants for hint function generation and discovery.
 *
 * `HINT_PACKAGE` is defined in [AutoProviderNaming] (derived from FIR layer).
 */
internal val HINT_FUNCTION_NAME: Name = Name.identifier("previewLabExport")

internal val HINT_VALUE_PARAM_NAME: Name = Name.identifier("value")

internal val HINT_FUNCTION_CALLABLE_ID: CallableId = CallableId(HINT_PACKAGE, HINT_FUNCTION_NAME)

internal val PREVIEW_EXPORT_FQN: FqName = FqName("me.tbsten.compose.preview.lab.PreviewExport")

internal val PREVIEW_EXPORT_HINT_FQN: FqName = FqName("me.tbsten.compose.preview.lab.PreviewExportHint")
