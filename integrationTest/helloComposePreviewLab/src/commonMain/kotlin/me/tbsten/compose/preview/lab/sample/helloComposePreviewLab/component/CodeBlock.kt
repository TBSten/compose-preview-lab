package me.tbsten.compose.preview.lab.sample.helloComposePreviewLab.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.SyntaxLanguage
import dev.snipme.kodeview.view.CodeTextView

@Composable
internal fun CodeBlock(code: String, language: SyntaxLanguage = SyntaxLanguage.KOTLIN, modifier: Modifier = Modifier) {
    val highlights by remember(code, language) {
        mutableStateOf(
            Highlights
                .Builder(code = code, language = language)
                .build(),
        )
    }

    CodeTextView(highlights = highlights, modifier = modifier)
}
