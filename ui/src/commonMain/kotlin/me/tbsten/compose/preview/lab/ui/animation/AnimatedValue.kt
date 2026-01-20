package me.tbsten.compose.preview.lab.ui.animation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi

@InternalComposePreviewLabApi
public fun <T> defaultAnimationSpec(): TweenSpec<T> = tween<T>(durationMillis = 100, easing = EaseOutExpo)

@Composable
@InternalComposePreviewLabApi
public fun Color.animated(): Color = animateColorAsState(
    targetValue = this,
    animationSpec = defaultAnimationSpec(),
).value

@Composable
@InternalComposePreviewLabApi
public fun Float.animated(): Float = animateFloatAsState(
    targetValue = this,
    animationSpec = defaultAnimationSpec(),
).value

@Composable
@InternalComposePreviewLabApi
public fun Dp.animated(): Dp = animateDpAsState(
    targetValue = this,
    animationSpec = defaultAnimationSpec(),
).value
