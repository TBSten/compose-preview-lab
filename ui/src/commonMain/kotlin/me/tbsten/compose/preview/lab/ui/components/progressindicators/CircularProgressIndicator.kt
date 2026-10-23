package me.tbsten.compose.preview.lab.ui.components.progressindicators

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max

@Composable
@UiComposePreviewLabApi
fun PreviewLabCircularProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = PreviewLabCircularProgressIndicatorDefaults.Color,
    trackColor: Color = PreviewLabCircularProgressIndicatorDefaults.TrackColor,
    strokeWidth: Dp = PreviewLabCircularProgressIndicatorDefaults.StrokeWidth,
    strokeCap: StrokeCap = PreviewLabCircularProgressIndicatorDefaults.StrokeStyle,
) {
    val coercedProgress = progress.coerceIn(0f, 1f)
    val stroke =
        with(LocalDensity.current) {
            Stroke(width = strokeWidth.toPx(), cap = strokeCap)
        }
    Canvas(
        modifier
            .progressSemantics(coercedProgress)
            .size(PreviewLabCircularProgressIndicatorDefaults.Diameter),
    ) {
        val startAngle = PreviewLabCircularProgressIndicatorDefaults.StartAngle
        val sweep = coercedProgress * PreviewLabCircularProgressIndicatorDefaults.SweepAngle
        drawCircularIndicatorTrack(trackColor, stroke)
        drawDeterminateCircularIndicator(startAngle, sweep, color, stroke)
    }
}

@Composable
@UiComposePreviewLabApi
fun PreviewLabCircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = PreviewLabCircularProgressIndicatorDefaults.Color,
    trackColor: Color = PreviewLabCircularProgressIndicatorDefaults.TrackColor,
    strokeWidth: Dp = PreviewLabCircularProgressIndicatorDefaults.StrokeWidth,
    strokeCap: StrokeCap = PreviewLabCircularProgressIndicatorDefaults.StrokeStyle,
) {
    val stroke =
        with(LocalDensity.current) {
            Stroke(width = strokeWidth.toPx(), cap = strokeCap)
        }

    val transition = rememberInfiniteTransition(label = "Indeterminate Transition")
    val currentRotation =
        transition.animateValue(
            0,
            PreviewLabCircularProgressIndicatorDefaults.RotationsPerCycle,
            Int.VectorConverter,
            infiniteRepeatable(
                animation =
                tween(
                    durationMillis =
                    PreviewLabCircularProgressIndicatorDefaults.RotationDuration *
                        PreviewLabCircularProgressIndicatorDefaults.RotationsPerCycle,
                    easing = LinearEasing,
                ),
            ),
            label = "Current Rotation",
        )
    val baseRotation =
        transition.animateFloat(
            0f,
            PreviewLabCircularProgressIndicatorDefaults.BaseRotationAngle,
            infiniteRepeatable(
                animation =
                tween(
                    durationMillis = PreviewLabCircularProgressIndicatorDefaults.RotationDuration,
                    easing = LinearEasing,
                ),
            ),
            label = "Base Rotation",
        )
    val endAngle =
        transition.animateFloat(
            0f,
            PreviewLabCircularProgressIndicatorDefaults.JumpRotationAngle,
            infiniteRepeatable(
                animation =
                keyframes {
                    durationMillis = PreviewLabCircularProgressIndicatorDefaults.HeadTailAnimationDuration +
                        PreviewLabCircularProgressIndicatorDefaults.HeadTailDelayDuration
                    0f at 0 using PreviewLabCircularProgressIndicatorDefaults.CircularEasing
                    PreviewLabCircularProgressIndicatorDefaults.JumpRotationAngle at
                        PreviewLabCircularProgressIndicatorDefaults.HeadTailAnimationDuration
                },
            ),
            label = "End Angle",
        )
    val startAngle =
        transition.animateFloat(
            0f,
            PreviewLabCircularProgressIndicatorDefaults.JumpRotationAngle,
            infiniteRepeatable(
                animation =
                keyframes {
                    durationMillis = PreviewLabCircularProgressIndicatorDefaults.HeadTailAnimationDuration +
                        PreviewLabCircularProgressIndicatorDefaults.HeadTailDelayDuration
                    0f at PreviewLabCircularProgressIndicatorDefaults.HeadTailDelayDuration using
                        PreviewLabCircularProgressIndicatorDefaults.CircularEasing
                    PreviewLabCircularProgressIndicatorDefaults.JumpRotationAngle at durationMillis
                },
            ),
            label = "Start Angle",
        )
    Canvas(
        modifier
            .progressSemantics()
            .size(PreviewLabCircularProgressIndicatorDefaults.Diameter),
    ) {
        drawCircularIndicatorTrack(trackColor, stroke)

        val currentRotationAngleOffset =
            (currentRotation.value * PreviewLabCircularProgressIndicatorDefaults.RotationAngleOffset) % 360f

        val sweep = abs(endAngle.value - startAngle.value)

        val offset =
            PreviewLabCircularProgressIndicatorDefaults.StartAngleOffset +
                currentRotationAngleOffset + baseRotation.value
        drawIndeterminateCircularIndicator(
            startAngle.value + offset,
            strokeWidth,
            sweep,
            color,
            stroke,
        )
    }
}

private fun DrawScope.drawCircularIndicator(startAngle: Float, sweep: Float, color: Color, stroke: Stroke,) {
    val diameterOffset = stroke.width / 2
    val arcDimen = size.width - 2 * diameterOffset
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweep,
        useCenter = false,
        topLeft = Offset(diameterOffset, diameterOffset),
        size = Size(arcDimen, arcDimen),
        style = stroke,
    )
}

private fun DrawScope.drawCircularIndicatorTrack(color: Color, stroke: Stroke,) =
    drawCircularIndicator(0f, PreviewLabCircularProgressIndicatorDefaults.SweepAngle, color, stroke)

private fun DrawScope.drawDeterminateCircularIndicator(startAngle: Float, sweep: Float, color: Color, stroke: Stroke,) =
    drawCircularIndicator(startAngle, sweep, color, stroke)

private fun DrawScope.drawIndeterminateCircularIndicator(
    startAngle: Float,
    strokeWidth: Dp,
    sweep: Float,
    color: Color,
    stroke: Stroke,
) {
    val strokeCapOffset =
        if (stroke.cap == StrokeCap.Butt) {
            0f
        } else {
            (180.0 / PI).toFloat() * (strokeWidth / (PreviewLabCircularProgressIndicatorDefaults.Diameter / 2)) / 2f
        }

    val adjustedStartAngle = startAngle + strokeCapOffset

    val adjustedSweep = max(sweep, 0.1f)

    drawCircularIndicator(adjustedStartAngle, adjustedSweep, color, stroke)
}

@UiComposePreviewLabApi
object PreviewLabCircularProgressIndicatorDefaults {
    val Color: Color
        @Composable get() = PreviewLabTheme.colors.primary

    val TrackColor: Color
        @Composable get() = PreviewLabTheme.colors.transparent

    private val Size = 48.dp
    private val ActiveIndicatorWidth = 2.dp
    val Diameter = Size - ActiveIndicatorWidth * 2

    val StrokeWidth = 4.dp
    val StrokeStyle: StrokeCap = StrokeCap.Square
    const val RotationsPerCycle = 5
    const val RotationDuration = 1332
    const val StartAngleOffset = -90f
    const val BaseRotationAngle = 286f
    const val JumpRotationAngle = 290f
    const val RotationAngleOffset = (BaseRotationAngle + JumpRotationAngle) % 360f
    const val SweepAngle = 360f
    const val StartAngle = 270f
    const val HeadTailAnimationDuration = (RotationDuration * 0.5).toInt()
    const val HeadTailDelayDuration = HeadTailAnimationDuration

    val CircularEasing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
}

@Composable
@Preview
private fun CircularProgressIndicatorPreview() {
    PreviewLabTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp),
        ) {
            BasicText(text = "Determinate Progress", style = PreviewLabTheme.typography.body1)
            PreviewLabCircularProgressIndicator(progress = 0.7f)

            BasicText(text = "Indeterminate Progress", style = PreviewLabTheme.typography.body1)
            PreviewLabCircularProgressIndicator()
        }
    }
}
