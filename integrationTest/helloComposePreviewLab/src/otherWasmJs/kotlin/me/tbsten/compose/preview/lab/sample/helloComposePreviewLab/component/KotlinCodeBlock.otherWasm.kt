package me.tbsten.compose.preview.lab.sample.helloComposePreviewLab.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.SyntaxLanguage
import generateAnnotatedString

@Composable
internal actual fun KotlinCodeBlock(code: String, modifier: Modifier, style: TextStyle, contentPadding: PaddingValues,) {
    val highlights by remember(code) {
        mutableStateOf(
            Highlights
                .Builder(code = code, language = SyntaxLanguage.KOTLIN)
                .build(),
        )
    }

    CodeTextView(highlights = highlights, modifier = modifier, style = style, contentPadding = contentPadding)
}

@Composable
private fun CodeTextView(
    highlights: Highlights,
    modifier: Modifier = Modifier.background(Color.Transparent),
    style: TextStyle = LocalTextStyle.current,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var textState by remember {
        mutableStateOf(AnnotatedString(highlights.getCode()))
    }

    LaunchedEffect(highlights) {
        textState = highlights
            .getHighlights()
            .generateAnnotatedString(highlights.getCode())
    }

    Surface(
        modifier = modifier,
        color = Color.Transparent,
    ) {
        Text(
            text = textState,
            style = style,
            modifier = modifier
                .horizontalScroll(rememberScrollState())
                .padding(contentPadding),
        )
    }
}
