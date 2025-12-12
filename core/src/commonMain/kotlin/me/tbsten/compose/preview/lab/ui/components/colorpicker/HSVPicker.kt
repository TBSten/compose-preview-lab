package me.tbsten.compose.preview.lab.ui.components.colorpicker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@Composable
internal fun HSVPicker(selectedColor: Color, onColorSelected: (Color) -> Unit, modifier: Modifier = Modifier) {
    val selectedColor = selectedColor.copy(alpha = 1f)
    val initialSelectedHue = remember { selectedColor }
    var rectSize by remember { mutableStateOf(IntSize.Zero) }
    var selectorPosition by remember { mutableStateOf(Offset.Zero) }
    val thumbSizePx = with(LocalDensity.current) { 6.dp.toPx() }
    val thumbColor = colorPickerThumbColor()

    fun updatePosition(newOffset: Offset) {
        selectorPosition =
            Offset(
                x = newOffset.x.coerceIn(0f, rectSize.width.toFloat() - thumbSizePx / 2),
                y = newOffset.y.coerceIn(0f, rectSize.height.toFloat() - thumbSizePx / 2),
            )
    }

    LaunchedEffect(rectSize, selectorPosition, selectedColor) {
        if (rectSize == IntSize.Zero || selectorPosition == Offset.Zero) {
            return@LaunchedEffect
        }
        val hue = selectedColor.toHueDegree()
        val saturation = selectorPosition.x / (rectSize.width - thumbSizePx / 2)
        val value = 1f - selectorPosition.y / (rectSize.height - thumbSizePx / 2)
        val color = Color.fromHsv(hue, saturation, value)
        onColorSelected(color)
    }

    LaunchedEffect(rectSize, initialSelectedHue) {
        if (rectSize == IntSize.Zero) {
            return@LaunchedEffect
        }
        val (_, saturation, value) = initialSelectedHue.toHsv()
        val x = saturation * (rectSize.width - thumbSizePx / 2)
        val y = (1f - value) * (rectSize.height - thumbSizePx / 2)
        selectorPosition = Offset(x, y)
    }

    Canvas(
        modifier =
            modifier
                .fillMaxSize()
                .onSizeChanged { rectSize = it }
                .pointerInput(Unit) { detectTapGestures { updatePosition(it) } }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ -> updatePosition(change.position) }
                },
    ) {
        val cornerRadius = 4.dp.toPx()

        drawRoundRect(
            Brush.horizontalGradient(listOf(Color.White, selectedColor)),
            cornerRadius = CornerRadius(x = cornerRadius, y = cornerRadius),
        )
        drawRoundRect(
            Brush.verticalGradient(listOf(Color.Transparent, Color.Black)),
            cornerRadius = CornerRadius(x = cornerRadius, y = cornerRadius),
        )
        drawCircle(
            color = thumbColor,
            style = Stroke(width = thumbSizePx / 2f),
            center = selectorPosition,
            radius = thumbSizePx,
        )
    }
}
