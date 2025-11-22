package me.tbsten.compose.preview.lab.sample.helloComposePreviewLab.component

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A reusable documentation page component that provides:
 * - MaterialTheme with dark/light mode support
 * - SelectionContainer for text selection
 * - Gradient background based on theme mode
 * - Scrollable column layout
 *
 * @param modifier Modifier to be applied to the Column
 * @param verticalSpacing Vertical spacing between child elements (default: 32.dp)
 * @param contentPadding Horizontal padding of the Column (default: 24.dp)
 * @param content The composable content of the page
 */
@Composable
fun DocPage(
    modifier: Modifier = Modifier,
    verticalSpacing: Dp = 32.dp,
    contentPadding: Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (isDark) darkColorScheme() else lightColorScheme(),
    ) {
        val backgroundGradient = createBackgroundGradient(isDark)

        SelectionContainer {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .background(backgroundGradient)
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding),
                verticalArrangement = Arrangement.spacedBy(verticalSpacing),
                content = content,
            )
        }
    }
}
