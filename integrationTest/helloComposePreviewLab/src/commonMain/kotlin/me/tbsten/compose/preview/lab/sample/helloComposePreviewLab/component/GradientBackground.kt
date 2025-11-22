package me.tbsten.compose.preview.lab.sample.helloComposePreviewLab.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Creates a vertical gradient background brush based on the Material Theme colors.
 *
 * @param isDark Whether the current theme is dark mode
 * @return A vertical gradient brush suitable for the current theme
 */
@Composable
fun createBackgroundGradient(isDark: Boolean): Brush = Brush.verticalGradient(
    colors = listOf(
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.primaryContainer,
    ),
)

/**
 * Creates a color for code block backgrounds using Material Theme.
 *
 * @return A color from Material Theme color scheme
 */
@Composable
fun createCodeBlockColor() = MaterialTheme.colorScheme.surface
