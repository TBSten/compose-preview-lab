package me.tbsten.compose.preview.lab.sample.helloComposePreviewLab.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

@Composable
internal expect fun KotlinCodeBlock(
    code: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    contentPadding: PaddingValues = PaddingValues.Zero,
)
