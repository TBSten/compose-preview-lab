package me.tbsten.compose.preview.lab.me

import androidx.compose.runtime.Composable

data class CollectedPreview(
    val displayName: String,
    val filePath: String,
    val content: @Composable () -> Unit,
)
