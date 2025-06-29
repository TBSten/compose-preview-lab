package me.tbsten.compose.preview.lab.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import me.tbsten.compose.preview.lab.core.generated.resources.Res
import me.tbsten.compose.preview.lab.core.generated.resources.icon_refresh
import me.tbsten.compose.preview.lab.core.generated.resources.icon_zoom_in
import me.tbsten.compose.preview.lab.core.generated.resources.icon_zoom_out
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Text
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun PreviewLabHeader(
    scale: Float,
    onScaleChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.clip(RectangleShape)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .background(PreviewLabTheme.colors.background)
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .height(IntrinsicSize.Min)
                .padding(8.dp)
                .zIndex(2f),
        ) {
            Zoom(
                scale = scale,
                onScaleChange = onScaleChange,
                modifier = Modifier
                    .fillMaxHeight(),
            )
        }

        Divider(
            Modifier
                .zIndex(2f),
        )

        content()
    }
}

@Composable
private fun Zoom(scale: Float, onScaleChange: (Float) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text("Zoom", style = PreviewLabTheme.typography.label2)

        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CommonIconButton(
                painter = painterResource(Res.drawable.icon_zoom_in),
                contentDescription = "Zoom In",
                enabled = scale < MaxZoomScale,
                onClick = {
                    onScaleChange(scale.nextZoomInScale())
                },
            )

            CommonIconButton(
                painter = painterResource(Res.drawable.icon_zoom_out),
                contentDescription = "Zoom Out",
                enabled = MinZoomScale < scale,
                onClick = {
                    onScaleChange(scale.nextZoomOutScale())
                },
                modifier = Modifier.weight(1f),
            )

            CommonIconButton(
                painter = painterResource(Res.drawable.icon_refresh),
                contentDescription = "Zoom Reset",
                onClick = {
                    onScaleChange(1.00f)
                },
            )
        }
    }
}

private const val MinZoomScale = 0.10f
private const val MaxZoomScale = 10.00f
private fun Float.nextZoomInScale(): Float = when (this) {
    in Float.MIN_VALUE..<1.0f -> this + 0.10f
    in 1.0f..<2.0f -> this + 0.25f
    in 1.0f..<Float.MAX_VALUE -> this + 1.00f
    else -> TODO("Zoom value is out of range: $this")
}

private fun Float.nextZoomOutScale(): Float = when (this) {
    in Float.MIN_VALUE..<1.0f -> this - 0.10f
    in 1.0f..<2.0f -> this - 0.25f
    in 1.0f..<Float.MAX_VALUE -> this - 1.00f
    else -> TODO("Zoom value is out of range: $this")
}
