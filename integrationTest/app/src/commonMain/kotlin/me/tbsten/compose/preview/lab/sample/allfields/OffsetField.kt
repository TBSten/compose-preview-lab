package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.OffsetField
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@ComposePreviewLabOption(id = "OffsetFieldExample")
@Composable
internal fun OffsetFieldExample() = PreviewLab {
    val offset = fieldValue { OffsetField("Position", Offset(50f, 100f)) }

    Box(
        modifier = Modifier
            .size(100.dp)
            .graphicsLayer {
                translationX = offset.x
                translationY = offset.y
            }
            .background(Color.Blue)
    )
}
