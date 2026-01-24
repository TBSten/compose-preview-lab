package me.tbsten.compose.preview.lab.ui.animation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi

@UiComposePreviewLabApi
fun <T> defaultAnimationSpec() = tween<T>(durationMillis = 100, easing = EaseOutExpo)

@Composable
@UiComposePreviewLabApi
fun Color.animated() = animateColorAsState(
    targetValue = this,
    animationSpec = defaultAnimationSpec(),
).value

@Composable
@UiComposePreviewLabApi
fun Float.animated() = animateFloatAsState(
    targetValue = this,
    animationSpec = defaultAnimationSpec(),
).value

@Composable
@UiComposePreviewLabApi
fun Dp.animated() = animateDpAsState(
    targetValue = this,
    animationSpec = defaultAnimationSpec(),
).value
