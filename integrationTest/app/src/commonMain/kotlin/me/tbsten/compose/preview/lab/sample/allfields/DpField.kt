package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.DpField
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@ComposePreviewLabOption(id = "DpFieldExample")
@Composable
internal fun DpFieldExample() = PreviewLab {
    Box(
        modifier = Modifier
            .padding(fieldValue { DpField("Padding", 16.dp) })
    ) {
        Text("Padded Content")
    }
}
