package me.tbsten.compose.preview.lab.previewlab.header

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.PreviewLabIconButton
import me.tbsten.compose.preview.lab.ui.components.PreviewLabText
import me.tbsten.compose.preview.lab.ui.generated.resources.PreviewLabUiRes
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_refresh
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_zoom_in
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_zoom_out
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun Zoom(scale: Float, onScaleChange: (Float) -> Unit, onOffsetReset: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        PreviewLabText("Zoom", style = PreviewLabTheme.typography.label2)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PreviewLabIconButton(
                painter = painterResource(PreviewLabUiRes.drawable.icon_zoom_in),
                contentDescription = "Zoom In",
                enabled = scale < MaxZoomScale,
                onClick = {
                    onScaleChange(scale.nextZoomInScale())
                },
            )

            PreviewLabIconButton(
                painter = painterResource(PreviewLabUiRes.drawable.icon_zoom_out),
                contentDescription = "Zoom Out",
                enabled = MinZoomScale < scale,
                onClick = {
                    onScaleChange(scale.nextZoomOutScale())
                },
                modifier = Modifier.weight(1f),
            )

            PreviewLabIconButton(
                painter = painterResource(PreviewLabUiRes.drawable.icon_refresh),
                contentDescription = "Zoom Reset",
                onClick = {
                    onScaleChange(1.00f)
                    onOffsetReset()
                },
            )
        }
    }
}

private const val MinZoomScale = 0.10f
private const val MaxZoomScale = 10.00f

/**
 * Calculates the next zoom-in scale from the current zoom ratio
 *
 * Applies appropriate increment amounts based on the ratio, supporting gradual
 * adjustments from fine-tuning to large adjustments. Below 1.0x increases by 0.1x,
 * between 1.0-2.0x increases by 0.25x, above 2.0x increases by 1.0x.
 *
 * @return Next zoom-in scale
 */
private fun Float.nextZoomInScale(): Float = when (this) {
    in Float.MIN_VALUE..<1.0f -> this + 0.10f
    in 1.0f..<2.0f -> this + 0.25f
    in 1.0f..<Float.MAX_VALUE -> this + 1.00f
    else -> TODO("Zoom value is out of range: $this")
}

/**
 * Calculates the next zoom-out scale from the current zoom ratio
 *
 * Applies appropriate decrement amounts based on the ratio, supporting gradual
 * adjustments from fine-tuning to large adjustments. Below 1.0x decreases by 0.1x,
 * between 1.0-2.0x decreases by 0.25x, above 2.0x decreases by 1.0x.
 *
 * @return Next zoom-out scale
 */
private fun Float.nextZoomOutScale(): Float = when (this) {
    in Float.MIN_VALUE..<1.0f -> this - 0.10f
    in 1.0f..<2.0f -> this - 0.25f
    in 1.0f..<Float.MAX_VALUE -> this - 1.00f
    else -> TODO("Zoom value is out of range: $this")
}
