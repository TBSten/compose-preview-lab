package me.tbsten.compose.preview.lab.extension.debugger.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.PreviewLabText

/**
 * Empty state displayed when no debug tools are registered.
 *
 * @param modifier Modifier to apply to the container
 */
private const val DebuggerDocsUrl =
    "https://tbsten.github.io/compose-preview-lab/integrationTest/main/js/docs/guides/extensions/debugger"

@Composable
internal fun DebugMenuEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        PreviewLabText(
            text = buildAnnotatedString {
                appendLine("No debug tools registered.")
                appendLine("Please register DebugMenu.tool { SomeDebugTool() }.")
                append("See ")

                appendLink(
                    url = DebuggerDocsUrl,
                    text = "debugger documentation",
                )

                append(".")
            },
            color = PreviewLabTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 2.em,
        )
    }
}

@Suppress("ComposableNaming")
@Composable
private fun AnnotatedString.Builder.appendLink(url: String, text: String = url) {
    withStyle(SpanStyle(color = PreviewLabTheme.colors.tertiary, textDecoration = TextDecoration.Underline)) {
        withLink(LinkAnnotation.Url(url)) {
            append(text)
        }
    }
}
