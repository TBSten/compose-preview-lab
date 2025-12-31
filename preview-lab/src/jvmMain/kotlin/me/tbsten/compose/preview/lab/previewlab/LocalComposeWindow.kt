package me.tbsten.compose.preview.lab.previewlab

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.awt.ComposeWindow
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi

@InternalComposePreviewLabApi
val LocalComposeWindow = staticCompositionLocalOf<ComposeWindow?> { null }
