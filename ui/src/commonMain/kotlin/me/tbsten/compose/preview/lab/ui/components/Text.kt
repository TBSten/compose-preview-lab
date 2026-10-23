package me.tbsten.compose.preview.lab.ui.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.composeunstyled.LocalContentColor
import com.composeunstyled.LocalTextStyle
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi

/**
 * Themed text built on [BasicText].
 *
 * Colour precedence follows Composables UI `Text`: an explicit [color] wins, then `style.color`, then the
 * surrounding `LocalContentColor`. [textAlign] defaults to [TextAlign.Unspecified] so `style.textAlign` is honoured.
 *
 * @param text Text to display.
 * @param modifier Modifier applied to the text.
 * @param color Colour of the text. [Color.Unspecified] falls back to `style.color`, then `LocalContentColor`.
 * @param fontSize Font size; [TextUnit.Unspecified] keeps the value from [style].
 * @param fontStyle Font style; `null` keeps the value from [style].
 * @param fontWeight Font weight; `null` keeps the value from [style].
 * @param fontFamily Font family; `null` keeps the value from [style].
 * @param letterSpacing Letter spacing; [TextUnit.Unspecified] keeps the value from [style].
 * @param textDecoration Text decoration; `null` keeps the value from [style].
 * @param textAlign Text alignment; [TextAlign.Unspecified] keeps the value from [style].
 * @param lineHeight Line height; [TextUnit.Unspecified] keeps the value from [style].
 * @param overflow How visual overflow is handled.
 * @param softWrap Whether the text should break at soft line breaks.
 * @param maxLines Maximum number of lines.
 * @param minLines Minimum number of lines.
 * @param onTextLayout Callback invoked when a new text layout is calculated.
 * @param style Base text style, defaults to `LocalTextStyle`.
 */
@Composable
@UiComposePreviewLabApi
fun PreviewLabText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign = TextAlign.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
) {
    PreviewLabText(
        text = AnnotatedString(text = text),
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = style,
    )
}

/**
 * Themed [AnnotatedString] text built on [BasicText]; supports [inlineContent].
 *
 * Colour precedence follows Composables UI `Text`: an explicit [color] wins, then `style.color`, then the
 * surrounding `LocalContentColor`. [textAlign] defaults to [TextAlign.Unspecified] so `style.textAlign` is honoured.
 *
 * @param text Text to display.
 * @param modifier Modifier applied to the text.
 * @param color Colour of the text. [Color.Unspecified] falls back to `style.color`, then `LocalContentColor`.
 * @param fontSize Font size; [TextUnit.Unspecified] keeps the value from [style].
 * @param fontStyle Font style; `null` keeps the value from [style].
 * @param fontWeight Font weight; `null` keeps the value from [style].
 * @param fontFamily Font family; `null` keeps the value from [style].
 * @param letterSpacing Letter spacing; [TextUnit.Unspecified] keeps the value from [style].
 * @param textDecoration Text decoration; `null` keeps the value from [style].
 * @param textAlign Text alignment; [TextAlign.Unspecified] keeps the value from [style].
 * @param lineHeight Line height; [TextUnit.Unspecified] keeps the value from [style].
 * @param overflow How visual overflow is handled.
 * @param softWrap Whether the text should break at soft line breaks.
 * @param maxLines Maximum number of lines.
 * @param minLines Minimum number of lines.
 * @param inlineContent Map of inline composables keyed by the placeholder ids used in [text].
 * @param onTextLayout Callback invoked when a new text layout is calculated.
 * @param style Base text style, defaults to `LocalTextStyle`.
 */
@Composable
@UiComposePreviewLabApi
fun PreviewLabText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign = TextAlign.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
) {
    // Explicit colour → style colour → content colour (Composables UI `Text` precedence).
    val resolvedColor = color.takeOrElse { style.color.takeOrElse { LocalContentColor.current } }
    val mergedStyle =
        style.merge(
            TextStyle(
                color = resolvedColor,
                fontSize = fontSize,
                fontWeight = fontWeight,
                textAlign = textAlign,
                lineHeight = lineHeight,
                fontFamily = fontFamily,
                textDecoration = textDecoration,
                fontStyle = fontStyle,
                letterSpacing = letterSpacing,
            ),
        )
    BasicText(
        text = text,
        modifier = modifier,
        style = mergedStyle,
        onTextLayout = onTextLayout,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        inlineContent = inlineContent,
    )
}
