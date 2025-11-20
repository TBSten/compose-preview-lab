package me.tbsten.compose.preview.lab.sample.helloComposePreviewLab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.imageResource

@OptIn(ExperimentalResourceApi::class)
@Composable
internal actual fun DrawableResource.preloadImageVector(): State<ImageBitmap?> = imageResource(this).let(::mutableStateOf)
