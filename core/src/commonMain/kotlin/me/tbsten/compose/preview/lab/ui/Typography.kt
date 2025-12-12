package me.tbsten.compose.preview.lab.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@InternalComposePreviewLabApi
fun fontFamily() = FontFamily.Default

@InternalComposePreviewLabApi
data class Typography(
    val h1: TextStyle,
    val h2: TextStyle,
    val h3: TextStyle,
    val h4: TextStyle,
    val body1: TextStyle,
    val body2: TextStyle,
    val body3: TextStyle,
    val label1: TextStyle,
    val label2: TextStyle,
    val label3: TextStyle,
    val button: TextStyle,
    val input: TextStyle,
)

private val defaultTypography =
    Typography(
        h1 =
        TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp,
        ),
        h2 =
        TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp,
        ),
        h3 =
        TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp,
        ),
        h4 =
        TextStyle(
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp,
        ),
        body1 =
        TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp,
        ),
        body2 =
        TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.15.sp,
        ),
        body3 =
        TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.15.sp,
        ),
        label1 =
        TextStyle(
            fontWeight = FontWeight.W500,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp,
        ),
        label2 =
        TextStyle(
            fontWeight = FontWeight.W500,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp,
        ),
        label3 =
        TextStyle(
            fontWeight = FontWeight.W500,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            letterSpacing = 0.5.sp,
        ),
        button =
        TextStyle(
            fontWeight = FontWeight.W500,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 1.sp,
        ),
        input =
        TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp,
        ),
    )

@Composable
@InternalComposePreviewLabApi
fun provideTypography(): Typography {
    val fontFamily = fontFamily()

    return defaultTypography.copy(
        h1 = defaultTypography.h1.copy(fontFamily = fontFamily),
        h2 = defaultTypography.h2.copy(fontFamily = fontFamily),
        h3 = defaultTypography.h3.copy(fontFamily = fontFamily),
        h4 = defaultTypography.h4.copy(fontFamily = fontFamily),
        body1 = defaultTypography.body1.copy(fontFamily = fontFamily),
        body2 = defaultTypography.body2.copy(fontFamily = fontFamily),
        body3 = defaultTypography.body3.copy(fontFamily = fontFamily),
        label1 = defaultTypography.label1.copy(fontFamily = fontFamily),
        label2 = defaultTypography.label2.copy(fontFamily = fontFamily),
        label3 = defaultTypography.label3.copy(fontFamily = fontFamily),
        button = defaultTypography.button.copy(fontFamily = fontFamily),
        input = defaultTypography.input.copy(fontFamily = fontFamily),
    )
}

@InternalComposePreviewLabApi
val LocalTypography = staticCompositionLocalOf { defaultTypography }
@InternalComposePreviewLabApi
val LocalTextStyle = compositionLocalOf(structuralEqualityPolicy()) { TextStyle.Default }

@Composable
@Preview
private fun DefaultTypographyPreview() {
    val typography = defaultTypography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BasicText(
            text = "H1 Heading",
            style = typography.h1,
        )
        BasicText(
            text = "H2 Heading",
            style = typography.h2,
        )
        BasicText(
            text = "H3 Heading",
            style = typography.h3,
        )
        BasicText(
            text = "H4 Heading",
            style = typography.h4,
        )

        Spacer(modifier = Modifier.height(8.dp))

        BasicText(
            text = "This is body1 text.",
            style = typography.body1,
        )
        BasicText(
            text = "This is body2 text.",
            style = typography.body2,
        )
        BasicText(
            text = "Body3 text for fine print.",
            style = typography.body3,
        )

        Spacer(modifier = Modifier.height(8.dp))

        BasicText(
            text = "Label1: Form Label",
            style = typography.label1,
        )
        BasicText(
            text = "Label2: Secondary Info",
            style = typography.label2,
        )
        BasicText(
            text = "Label3: Tiny Details",
            style = typography.label3,
        )

        Spacer(modifier = Modifier.height(8.dp))

        BasicText(
            text = "BUTTON TEXT",
            style = typography.button,
        )
        BasicText(
            text = "Input text field",
            style = typography.input,
        )
    }
}
