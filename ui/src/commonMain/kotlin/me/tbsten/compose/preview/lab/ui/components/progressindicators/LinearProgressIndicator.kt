package me.tbsten.compose.preview.lab.ui.components.progressindicators

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.abs

@Composable
@UiComposePreviewLabApi
fun PreviewLabLinearProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = PreviewLabLinearProgressIndicatorDefaults.Color,
    trackColor: Color = PreviewLabLinearProgressIndicatorDefaults.TrackColor,
    strokeCap: StrokeCap = PreviewLabLinearProgressIndicatorDefaults.StrokeStyle,
) {
    val coercedProgress = progress.coerceIn(0f, 1f)
    Canvas(
        modifier
            .progressSemantics(coercedProgress)
            .height(PreviewLabLinearProgressIndicatorDefaults.TrackHeight)
            .fillMaxWidth(),
    ) {
        val strokeWidth = size.height
        drawLinearIndicatorTrack(trackColor, strokeWidth, strokeCap)
        drawLinearIndicator(0f, coercedProgress, color, strokeWidth, strokeCap)
    }
}

@Composable
@UiComposePreviewLabApi
fun PreviewLabLinearProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = PreviewLabLinearProgressIndicatorDefaults.Color,
    trackColor: Color = PreviewLabLinearProgressIndicatorDefaults.TrackColor,
    strokeCap: StrokeCap = PreviewLabLinearProgressIndicatorDefaults.StrokeStyle,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "InfiniteTransition")
    val firstLineHead =
        infiniteTransition.animateFloat(
            0f,
            1f,
            infiniteRepeatable(
                animation =
                keyframes {
                    durationMillis = PreviewLabLinearProgressIndicatorDefaults.AnimationDuration
                    0f at PreviewLabLinearProgressIndicatorDefaults.FirstLineHeadDelay using
                        PreviewLabLinearProgressIndicatorDefaults.FirstLineHeadEasing
                    1f at
                        PreviewLabLinearProgressIndicatorDefaults.FirstLineHeadDuration +
                        PreviewLabLinearProgressIndicatorDefaults.FirstLineHeadDelay
                },
            ),
            label = "FirstLineHead",
        )
    val firstLineTail =
        infiniteTransition.animateFloat(
            0f,
            1f,
            infiniteRepeatable(
                animation =
                keyframes {
                    durationMillis = PreviewLabLinearProgressIndicatorDefaults.AnimationDuration
                    0f at PreviewLabLinearProgressIndicatorDefaults.FirstLineTailDelay using
                        PreviewLabLinearProgressIndicatorDefaults.FirstLineTailEasing
                    1f at
                        PreviewLabLinearProgressIndicatorDefaults.FirstLineTailDuration +
                        PreviewLabLinearProgressIndicatorDefaults.FirstLineTailDelay
                },
            ),
            label = "FirstLineTail",
        )
    val secondLineHead =
        infiniteTransition.animateFloat(
            0f,
            1f,
            infiniteRepeatable(
                animation =
                keyframes {
                    durationMillis = PreviewLabLinearProgressIndicatorDefaults.AnimationDuration
                    0f at PreviewLabLinearProgressIndicatorDefaults.SecondLineHeadDelay using
                        PreviewLabLinearProgressIndicatorDefaults.SecondLineHeadEasing
                    1f at
                        PreviewLabLinearProgressIndicatorDefaults.SecondLineHeadDuration +
                        PreviewLabLinearProgressIndicatorDefaults.SecondLineHeadDelay
                },
            ),
            label = "SecondLineHead",
        )
    val secondLineTail =
        infiniteTransition.animateFloat(
            0f,
            1f,
            infiniteRepeatable(
                animation =
                keyframes {
                    durationMillis = PreviewLabLinearProgressIndicatorDefaults.AnimationDuration
                    0f at PreviewLabLinearProgressIndicatorDefaults.SecondLineTailDelay using
                        PreviewLabLinearProgressIndicatorDefaults.SecondLineTailEasing
                    1f at
                        PreviewLabLinearProgressIndicatorDefaults.SecondLineTailDuration +
                        PreviewLabLinearProgressIndicatorDefaults.SecondLineTailDelay
                },
            ),
            label = "SecondLineTail",
        )
    Canvas(
        modifier
            .progressSemantics()
            .height(PreviewLabLinearProgressIndicatorDefaults.TrackHeight)
            .fillMaxWidth(),
    ) {
        val strokeWidth = size.height
        drawLinearIndicatorTrack(trackColor, strokeWidth, strokeCap)
        if (firstLineHead.value - firstLineTail.value > 0) {
            drawLinearIndicator(
                firstLineHead.value,
                firstLineTail.value,
                color,
                strokeWidth,
                strokeCap,
            )
        }
        if (secondLineHead.value - secondLineTail.value > 0) {
            drawLinearIndicator(
                secondLineHead.value,
                secondLineTail.value,
                color,
                strokeWidth,
                strokeCap,
            )
        }
    }
}

private fun DrawScope.drawLinearIndicator(
    startFraction: Float,
    endFraction: Float,
    color: Color,
    strokeWidth: Float,
    strokeCap: StrokeCap,
) {
    val width = size.width
    val height = size.height
    val yOffset = height / 2

    val isLtr = layoutDirection == LayoutDirection.Ltr
    val barStart = (if (isLtr) startFraction else 1f - endFraction) * width
    val barEnd = (if (isLtr) endFraction else 1f - startFraction) * width

    if (strokeCap == StrokeCap.Butt || height > width) {
        drawLine(color, Offset(barStart, yOffset), Offset(barEnd, yOffset), strokeWidth)
    } else {
        val strokeCapOffset = strokeWidth / 2
        val coerceRange = strokeCapOffset..(width - strokeCapOffset)
        val adjustedBarStart = barStart.coerceIn(coerceRange)
        val adjustedBarEnd = barEnd.coerceIn(coerceRange)

        if (abs(endFraction - startFraction) > 0) {
            drawLine(
                color,
                Offset(adjustedBarStart, yOffset),
                Offset(adjustedBarEnd, yOffset),
                strokeWidth,
                strokeCap,
            )
        }
    }
}

private fun DrawScope.drawLinearIndicatorTrack(color: Color, strokeWidth: Float, strokeCap: StrokeCap,) =
    drawLinearIndicator(0f, 1f, color, strokeWidth, strokeCap)

@UiComposePreviewLabApi
object PreviewLabLinearProgressIndicatorDefaults {
    val Color: Color
        @Composable get() = PreviewLabTheme.colors.primary

    val TrackColor: Color
        @Composable get() = PreviewLabTheme.colors.transparent

    val TrackHeight = 4.dp
    val StrokeStyle: StrokeCap = StrokeCap.Round
    const val AnimationDuration = 1800

    const val FirstLineHeadDuration = 750
    const val FirstLineTailDuration = 850
    const val SecondLineHeadDuration = 567
    const val SecondLineTailDuration = 533

    const val FirstLineHeadDelay = 0
    const val FirstLineTailDelay = 333
    const val SecondLineHeadDelay = 1000
    const val SecondLineTailDelay = 1267

    val FirstLineHeadEasing = CubicBezierEasing(0.2f, 0f, 0.8f, 1f)
    val FirstLineTailEasing = CubicBezierEasing(0.4f, 0f, 1f, 1f)
    val SecondLineHeadEasing = CubicBezierEasing(0f, 0f, 0.65f, 1f)
    val SecondLineTailEasing = CubicBezierEasing(0.1f, 0f, 0.45f, 1f)
}

@Composable
@Preview
private fun LinearProgressIndicatorPreview() {
    PreviewLabTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp),
        ) {
            BasicText(text = "Determinate Progress", style = PreviewLabTheme.typography.body1)
            PreviewLabLinearProgressIndicator(progress = 0.7f)

            BasicText(text = "Indeterminate Progress", style = PreviewLabTheme.typography.body1)
            PreviewLabLinearProgressIndicator()
        }
    }
}
