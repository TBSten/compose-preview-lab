package me.tbsten.compose.preview.lab

import androidx.compose.runtime.Composable

interface CollectedPreview {
    val displayName: String
    val filePath: String?
    val startLineNumber: Int?
    val code: String?
    val content: @Composable () -> Unit
}

internal data class CollectedPreviewImpl(
    override val displayName: String,
    override val filePath: String?,
    override val startLineNumber: Int? = null,
    override val code: String? = null,
    override val content: @Composable () -> Unit,
) : CollectedPreview

fun CollectedPreview(
    displayName: String,
    filePath: String?,
    startLineNumber: Int? = null,
    code: String? = null,
    content: @Composable () -> Unit,
): CollectedPreview = CollectedPreviewImpl(
    displayName = displayName,
    filePath = filePath,
    startLineNumber = startLineNumber,
    code = code,
    content = content,
)
