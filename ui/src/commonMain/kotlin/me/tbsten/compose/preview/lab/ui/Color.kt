package me.tbsten.compose.preview.lab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi

@UiComposePreviewLabApi
val Black: Color = Color(0xFF000000)

@UiComposePreviewLabApi
val Gray900: Color = Color(0xFF282828)

@UiComposePreviewLabApi
val Gray800: Color = Color(0xFF4b4b4b)

@UiComposePreviewLabApi
val Gray700: Color = Color(0xFF5e5e5e)

@UiComposePreviewLabApi
val Gray600: Color = Color(0xFF727272)

@UiComposePreviewLabApi
val Gray500: Color = Color(0xFF868686)

@UiComposePreviewLabApi
val Gray400: Color = Color(0xFFC7C7C7)

@UiComposePreviewLabApi
val Gray300: Color = Color(0xFFDFDFDF)

@UiComposePreviewLabApi
val Gray200: Color = Color(0xFFE2E2E2)

@UiComposePreviewLabApi
val Gray100: Color = Color(0xFFF7F7F7)

@UiComposePreviewLabApi
val Gray50: Color = Color(0xFFFFFFFF)

@UiComposePreviewLabApi
val White: Color = Color(0xFFFFFFFF)

@UiComposePreviewLabApi
val Red900: Color = Color(0xFF520810)

@UiComposePreviewLabApi
val Red800: Color = Color(0xFF950f22)

@UiComposePreviewLabApi
val Red700: Color = Color(0xFFbb032a)

@UiComposePreviewLabApi
val Red600: Color = Color(0xFFde1135)

@UiComposePreviewLabApi
val Red500: Color = Color(0xFFf83446)

@UiComposePreviewLabApi
val Red400: Color = Color(0xFFfc7f79)

@UiComposePreviewLabApi
val Red300: Color = Color(0xFFffb2ab)

@UiComposePreviewLabApi
val Red200: Color = Color(0xFFffd2cd)

@UiComposePreviewLabApi
val Red100: Color = Color(0xFFffe1de)

@UiComposePreviewLabApi
val Red50: Color = Color(0xFFfff0ee)

@UiComposePreviewLabApi
val Blue900: Color = Color(0xFF276EF1)

@UiComposePreviewLabApi
val Blue800: Color = Color(0xFF3F7EF2)

@UiComposePreviewLabApi
val Blue700: Color = Color(0xFF578EF4)

@UiComposePreviewLabApi
val Blue600: Color = Color(0xFF6F9EF5)

@UiComposePreviewLabApi
val Blue500: Color = Color(0xFF87AEF7)

@UiComposePreviewLabApi
val Blue400: Color = Color(0xFF9FBFF8)

@UiComposePreviewLabApi
val Blue300: Color = Color(0xFFB7CEFA)

@UiComposePreviewLabApi
val Blue200: Color = Color(0xFFCFDEFB)

@UiComposePreviewLabApi
val Blue100: Color = Color(0xFFE7EEFD)

@UiComposePreviewLabApi
val Blue50: Color = Color(0xFFFFFFFF)

@UiComposePreviewLabApi
val Green950: Color = Color(0xFF0B4627)

@UiComposePreviewLabApi
val Green900: Color = Color(0xFF16643B)

@UiComposePreviewLabApi
val Green800: Color = Color(0xFF1A7544)

@UiComposePreviewLabApi
val Green700: Color = Color(0xFF178C4E)

@UiComposePreviewLabApi
val Green600: Color = Color(0xFF1DAF61)

@UiComposePreviewLabApi
val Green500: Color = Color(0xFF1FC16B)

@UiComposePreviewLabApi
val Green400: Color = Color(0xFF3EE089)

@UiComposePreviewLabApi
val Green300: Color = Color(0xFF84EBB4)

@UiComposePreviewLabApi
val Green200: Color = Color(0xFFC2F5DA)

@UiComposePreviewLabApi
val Green100: Color = Color(0xFFD0FBE9)

@UiComposePreviewLabApi
val Green50: Color = Color(0xFFE0FAEC)

@Immutable
@UiComposePreviewLabApi
data class Colors(
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val error: Color,
    val onError: Color,
    val success: Color,
    val onSuccess: Color,
    val disabled: Color,
    val onDisabled: Color,
    val surface: Color,
    val onSurface: Color,
    val background: Color,
    val onBackground: Color,
    val outline: Color,
    val outlineSecondary: Color,
    val transparent: Color = Color.Transparent,
    val white: Color = White,
    val black: Color = Black,
    val text: Color,
    val textSecondary: Color,
    val textDisabled: Color,
    val scrim: Color,
    val elevation: Color,
)

@UiComposePreviewLabApi
val LightColors =
    Colors(
        primary = Black,
        onPrimary = White,
        secondary = Gray400,
        onSecondary = Black,
        tertiary = Blue900,
        onTertiary = White,
        surface = Gray200,
        onSurface = Black,
        error = Red600,
        onError = White,
        success = Green600,
        onSuccess = White,
        disabled = Gray100,
        onDisabled = Gray500,
        background = White,
        onBackground = Black,
        outline = Gray300,
        outlineSecondary = Gray100,
        transparent = Color.Transparent,
        white = White,
        black = Black,
        text = Black,
        textSecondary = Gray700,
        textDisabled = Gray400,
        scrim = Color.Black.copy(alpha = 0.32f),
        elevation = Gray700,
    )

@UiComposePreviewLabApi
val DarkColors =
    Colors(
        primary = White,
        onPrimary = Black,
        secondary = Gray400,
        onSecondary = White,
        tertiary = Blue300,
        onTertiary = Black,
        surface = Gray900,
        onSurface = White,
        error = Red400,
        onError = Black,
        success = Green700,
        onSuccess = Black,
        disabled = Gray700,
        onDisabled = Gray500,
        background = Black,
        onBackground = White,
        outline = Gray800,
        outlineSecondary = Gray600,
        transparent = Color.Transparent,
        white = White,
        black = Black,
        text = White,
        textSecondary = Gray300,
        textDisabled = Gray600,
        scrim = Color.Black.copy(alpha = 0.72f),
        elevation = Gray200,
    )

@UiComposePreviewLabApi
val LocalColors = staticCompositionLocalOf { LightColors }

@UiComposePreviewLabApi
val LocalContentColor = compositionLocalOf { Color.Black }

@UiComposePreviewLabApi
val LocalContentAlpha = compositionLocalOf { 1f }

@UiComposePreviewLabApi
fun Colors.contentColorFor(backgroundColor: Color): Color = when (backgroundColor) {
    primary -> onPrimary
    secondary -> onSecondary
    tertiary -> onTertiary
    surface -> onSurface
    error -> onError
    success -> onSuccess
    disabled -> onDisabled
    background -> onBackground
    else -> Color.Unspecified
}

@Preview
@Composable
private fun LightColorsPreview() {
    ColorsPreview(colors = LightColors, title = "Light Colors")
}

@Preview
@Composable
private fun DarkColorsPreview() {
    ColorsPreview(colors = DarkColors, title = "Dark Colors")
}

@Composable
private fun ColorsPreview(colors: Colors, title: String) {
    val colorItems = listOf(
        "primary" to colors.primary,
        "onPrimary" to colors.onPrimary,
        "secondary" to colors.secondary,
        "onSecondary" to colors.onSecondary,
        "tertiary" to colors.tertiary,
        "onTertiary" to colors.onTertiary,
        "error" to colors.error,
        "onError" to colors.onError,
        "success" to colors.success,
        "onSuccess" to colors.onSuccess,
        "disabled" to colors.disabled,
        "onDisabled" to colors.onDisabled,
        "surface" to colors.surface,
        "onSurface" to colors.onSurface,
        "background" to colors.background,
        "onBackground" to colors.onBackground,
        "outline" to colors.outline,
        "outlineSecondary" to colors.outlineSecondary,
        "text" to colors.text,
        "textSecondary" to colors.textSecondary,
        "textDisabled" to colors.textDisabled,
        "scrim" to colors.scrim,
        "elevation" to colors.elevation,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        BasicText(
            text = title,
            style = TextStyle(fontSize = 20.sp, color = colors.text),
        )
        colorItems.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowItems.forEach { (name, color) ->
                    Box(modifier = Modifier.weight(1f)) {
                        ColorSwatch(name = name, color = color, textColor = colors.text)
                    }
                }
                if (rowItems.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(name: String, color: Color, textColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color)
                .border(1.dp, Gray500),
        )
        BasicText(
            text = name,
            style = TextStyle(fontSize = 12.sp, color = textColor),
        )
    }
}

@Preview
@Composable
private fun PrimitiveColorsPreview() {
    val grayColors = listOf(
        "Black" to Black,
        "Gray900" to Gray900,
        "Gray800" to Gray800,
        "Gray700" to Gray700,
        "Gray600" to Gray600,
        "Gray500" to Gray500,
        "Gray400" to Gray400,
        "Gray300" to Gray300,
        "Gray200" to Gray200,
        "Gray100" to Gray100,
        "Gray50" to Gray50,
        "White" to White,
    )
    val redColors = listOf(
        "Red900" to Red900,
        "Red800" to Red800,
        "Red700" to Red700,
        "Red600" to Red600,
        "Red500" to Red500,
        "Red400" to Red400,
        "Red300" to Red300,
        "Red200" to Red200,
        "Red100" to Red100,
        "Red50" to Red50,
    )
    val blueColors = listOf(
        "Blue900" to Blue900,
        "Blue800" to Blue800,
        "Blue700" to Blue700,
        "Blue600" to Blue600,
        "Blue500" to Blue500,
        "Blue400" to Blue400,
        "Blue300" to Blue300,
        "Blue200" to Blue200,
        "Blue100" to Blue100,
        "Blue50" to Blue50,
    )
    val greenColors = listOf(
        "Green950" to Green950,
        "Green900" to Green900,
        "Green800" to Green800,
        "Green700" to Green700,
        "Green600" to Green600,
        "Green500" to Green500,
        "Green400" to Green400,
        "Green300" to Green300,
        "Green200" to Green200,
        "Green100" to Green100,
        "Green50" to Green50,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BasicText(
            text = "Primitive Colors",
            style = TextStyle(fontSize = 20.sp),
        )
        PrimitiveColorSection(title = "Gray", colors = grayColors)
        PrimitiveColorSection(title = "Red", colors = redColors)
        PrimitiveColorSection(title = "Blue", colors = blueColors)
        PrimitiveColorSection(title = "Green", colors = greenColors)
    }
}

@Composable
private fun PrimitiveColorSection(title: String, colors: List<Pair<String, Color>>) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        BasicText(
            text = title,
            style = TextStyle(fontSize = 14.sp),
        )
        colors.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth().height(24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowItems.forEach { (name, color) ->
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(color)
                                .border(1.dp, Gray500),
                        )
                        BasicText(
                            text = name,
                            style = TextStyle(fontSize = 10.sp),
                        )
                    }
                }
                if (rowItems.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
