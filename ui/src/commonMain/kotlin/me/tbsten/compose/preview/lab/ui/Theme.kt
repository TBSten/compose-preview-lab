package me.tbsten.compose.preview.lab.ui

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.foundation.ripple

@UiComposePreviewLabApi
object PreviewLabTheme {
    val colors: Colors
        @ReadOnlyComposable @Composable
        get() = LocalColors.current

    val typography: Typography
        @ReadOnlyComposable @Composable
        get() = LocalTypography.current
}

@Composable
@UiComposePreviewLabApi
fun PreviewLabTheme(isDarkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val rippleIndication = ripple()
    val selectionColors = rememberTextSelectionColors(LightColors)
    val typography = provideTypography()
    val colors = if (isDarkTheme) DarkColors else LightColors

    CompositionLocalProvider(
        LocalColors provides colors,
        LocalTypography provides typography,
        LocalIndication provides rippleIndication,
        LocalTextSelectionColors provides selectionColors,
        LocalContentColor provides colors.contentColorFor(colors.background),
        LocalTextStyle provides typography.body1,
        content = content,
    )
}

@Composable
@UiComposePreviewLabApi
fun contentColorFor(color: Color): Color = PreviewLabTheme.colors.contentColorFor(color)

@Composable
@UiComposePreviewLabApi
fun rememberTextSelectionColors(colorScheme: Colors): TextSelectionColors {
    val primaryColor = colorScheme.primary
    return remember(primaryColor) {
        TextSelectionColors(
            handleColor = primaryColor,
            backgroundColor = primaryColor.copy(alpha = TextSelectionBackgroundOpacity),
        )
    }
}

@UiComposePreviewLabApi
const val TextSelectionBackgroundOpacity = 0.4f
