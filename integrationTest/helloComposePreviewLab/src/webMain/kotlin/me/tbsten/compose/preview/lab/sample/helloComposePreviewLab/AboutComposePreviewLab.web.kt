package me.tbsten.compose.preview.lab.sample.helloComposePreviewLab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.ImageBitmap
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.preloadImageBitmap

@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun DrawableResource.preloadImageVector(): State<ImageBitmap?> = preloadImageBitmap(this)
