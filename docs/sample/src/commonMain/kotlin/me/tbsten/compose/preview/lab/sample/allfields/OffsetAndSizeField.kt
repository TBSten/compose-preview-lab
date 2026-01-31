package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.field.DpOffsetField
import me.tbsten.compose.preview.lab.field.DpSizeField
import me.tbsten.compose.preview.lab.field.OffsetField
import me.tbsten.compose.preview.lab.field.SizeField
import me.tbsten.compose.preview.lab.sample.component.SamplePreviewLab
import androidx.compose.ui.tooling.preview.Preview

/**
 * Demonstrates [OffsetField], [DpOffsetField], [SizeField], and [DpSizeField].
 *
 * - [OffsetField]: Float-based coordinates (x, y) for Canvas/graphics
 * - [DpOffsetField]: Dp-based offset for layout positioning
 * - [SizeField]: Float-based dimensions for Canvas drawing
 * - [DpSizeField]: Dp-based dimensions for component sizing
 */
@Preview
@ComposePreviewLabOption(id = "OffsetAndSizeFieldExample")
@Composable
internal fun OffsetAndSizeFieldExample() = SamplePreviewLab {
    // OffsetField: Float 単位の座標
    val offset = fieldValue { OffsetField("Position", Offset(20f, 10f)) }

    // DpOffsetField: Dp 単位の座標
    val dpOffset = fieldValue { DpOffsetField("Text Offset", DpOffset(16.dp, 8.dp)) }

    // SizeField: Float 単位のサイズ
    val canvasSize = fieldValue { SizeField("Canvas Size", Size(80f, 60f)) }

    // DpSizeField: Dp 単位のサイズ
    val buttonSize = fieldValue { DpSizeField("Button Size", DpSize(100.dp, 40.dp)) }

    Column {
        Box(
            modifier = Modifier
                .size(50.dp)
                .graphicsLayer {
                    translationX = offset.x
                    translationY = offset.y
                }
                .background(Color.Blue),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Positioned Text",
            modifier = Modifier.offset(dpOffset.x, dpOffset.y),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Canvas(modifier = Modifier.size(100.dp)) {
            drawRect(Color.Green, size = canvasSize)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { },
            modifier = Modifier.size(buttonSize),
        ) {
            Text("Button")
        }
    }
}
