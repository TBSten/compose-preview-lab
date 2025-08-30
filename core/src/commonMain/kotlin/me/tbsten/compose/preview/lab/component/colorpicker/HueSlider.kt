package me.tbsten.compose.preview.lab.component.colorpicker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize

@Composable
internal fun HueSlider(selectedColor: Color, onColorSelected: (Color) -> Unit, modifier: Modifier = Modifier) {
    val initialSelectedHue = remember { selectedColor }
    var sliderSize by remember { mutableStateOf(Size.Zero) }
    var thumbPositionY by remember { mutableStateOf(0f) }
    val thumbHeightPx = with(LocalDensity.current) { 8.dp.toPx() }
    val thumbColor = colorPickerThumbColor()
    val updateColor by rememberUpdatedState(onColorSelected)

    fun onThumbPositionChange(newOffset: Offset) {
        val start = thumbHeightPx / 2
        val end = sliderSize.height - thumbHeightPx / 2
        val y = newOffset.y.coerceIn(start..end)
        val newPosition = y - start

        // calculate hue value based on new position value
        val hueDegreeAtPosition =
            (newPosition / (sliderSize.height - thumbHeightPx) * 360f).coerceIn(0f, 360f)
        val newColor = hueDegreeToColor(hueDegreeAtPosition)

        thumbPositionY = newPosition
        updateColor(newColor)
    }

    LaunchedEffect(sliderSize, initialSelectedHue) {
        if (!sliderSize.isEmpty()) {
            val deg = initialSelectedHue.toHueDegree()
            thumbPositionY = (deg / 360f) * (sliderSize.height - thumbHeightPx)
        }
    }

    Box(
        modifier =
        modifier
            .fillMaxHeight()
            .onSizeChanged { sliderSize = it.toSize() }
            .pointerInput(Unit) { detectTapGestures(onTap = ::onThumbPositionChange) }
            .pointerInput(Unit) {
                detectDragGestures { change, _ -> onThumbPositionChange(change.position) }
            },
    ) {
        // color track
        Canvas(
            modifier =
            Modifier.fillMaxSize()
                .padding(horizontal = 4.dp)
                .clip(RoundedCornerShape(4.dp)),
        ) {
            drawRect(Brush.verticalGradient(Color.colorList))
        }

        // draw thumb only when we know the size
        if (!sliderSize.isEmpty()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRoundRect(
                    color = thumbColor,
                    topLeft = Offset(x = (4.dp.toPx()) / 2, y = thumbPositionY),
                    size = Size(
                        width = sliderSize.width - (4.dp.toPx()),
                        height = thumbHeightPx,
                    ),
                    style = Stroke(width = 4.dp.toPx()),
                    cornerRadius = CornerRadius(6f, 6f),
                )
            }
        }
    }
}

private val Color.Companion.colorList by lazy {
    IntArray(360) { it }.map { deg -> hueDegreeToColor(deg.toFloat()) }
}
