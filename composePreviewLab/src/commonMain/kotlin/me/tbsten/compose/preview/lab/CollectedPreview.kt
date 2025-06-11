package me.tbsten.compose.preview.lab

import androidx.compose.runtime.Composable

data class CollectedPreview(
    val displayName: String,
    val filePath: String,
    val code: String? = null,
    val content: @Composable () -> Unit,
)
