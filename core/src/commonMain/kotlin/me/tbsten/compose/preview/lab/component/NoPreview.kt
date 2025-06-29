package me.tbsten.compose.preview.lab.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Text

@Composable
internal fun NoPreview() = SelectionContainer {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        modifier = Modifier
            .background(
                Brush.horizontalGradient(
                    0.4f to PreviewLabTheme.colors.error,
                    1.0f to PreviewLabTheme.colors.error.copy(alpha = 0.5f),
                ),
            )
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
    ) {
        Text(
            text = "🚨 No previews found.",
            style = PreviewLabTheme.typography.h2,
            color = PreviewLabTheme.colors.error,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Compose Preview Lab Gradle Plugin couldn't locate @Previews in PreviewLabRoot.previews.",
            style = PreviewLabTheme.typography.h3,
            color = PreviewLabTheme.colors.error,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(48.dp))
        Divider(color = PreviewLabTheme.colors.onError.copy(alpha = 0.25f))
        Text(
            noPreviewDetail,
            style = PreviewLabTheme.typography.body2,
            color = PreviewLabTheme.colors.onError,
        )
    }
}

private val noPreviewDetail = buildAnnotatedString {
    appendLine("PreviewLabRoot.previews return no @Previews. ")
    appendLine("Check the following:")

    withStyle(ParagraphStyle(textIndent = TextIndent(firstLine = 20.sp, restLine = 20.sp))) {
        append("✅ Check: Ensure there’s a @Preview function in your ")
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append("`commonMain` source set (src/commonMain/kotlin/)")
        }
        append(". Compose Preview Lab supports only commonMain Previews.")
        appendLine()
    }

    append("If the error persists, report it on ")
    withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
        withLink(LinkAnnotation.Url("https://github.com/TBSten/compose-preview-lab/issues")) {
            append("GitHub issues")
        }
    }
    append(". Try running ")
    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
        append("`./gradlew generatePreviewSources`")
    }
    append(" before the app if needed.")
    appendLine()
}
