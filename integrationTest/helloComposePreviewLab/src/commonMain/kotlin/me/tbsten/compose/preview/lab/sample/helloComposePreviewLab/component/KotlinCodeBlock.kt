package me.tbsten.compose.preview.lab.sample.helloComposePreviewLab.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal expect fun KotlinCodeBlock(code: String, modifier: Modifier = Modifier)
