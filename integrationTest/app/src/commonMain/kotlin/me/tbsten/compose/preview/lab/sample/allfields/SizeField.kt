package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.SizeField
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@ComposePreviewLabOption(id = "SizeFieldExample")
@Composable
internal fun SizeFieldExample() = PreviewLab {
    val canvasSize = fieldValue { SizeField("Canvas", Size(200f, 150f)) }

    Canvas(
        modifier = Modifier.size(200.dp)
    ) {
        drawRect(Color.Blue, size = canvasSize)
    }
}
