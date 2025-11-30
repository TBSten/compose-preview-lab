package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.DpOffsetField
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@ComposePreviewLabOption(id = "DpOffsetFieldExample")
@Composable
internal fun DpOffsetFieldExample() = PreviewLab {
    val offset = fieldValue { DpOffsetField("Offset", DpOffset(16.dp, 8.dp)) }

    Text(
        text = "Positioned Text",
        modifier = Modifier.offset(offset.x, offset.y)
    )
}
