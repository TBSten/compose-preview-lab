package me.tbsten.compose.preview.lab.sample.helloComposePreviewLab.component

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Creates a vertical gradient background brush based on the theme mode.
 *
 * @param isDark Whether the current theme is dark mode
 * @return A vertical gradient brush suitable for the current theme
 */
fun createBackgroundGradient(isDark: Boolean): Brush = if (isDark) {
    Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A1A2E),
            Color(0xFF16213E),
            Color(0xFF0F3460),
        ),
    )
} else {
    Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF8F9FA),
            Color(0xFFE8EAF6),
            Color(0xFFE3F2FD),
        ),
    )
}

/**
 * Creates a linear gradient brush for code block backgrounds.
 *
 * @return A linear gradient brush from grey to lavender
 */
fun createCodeBlockColor() = Color.White
