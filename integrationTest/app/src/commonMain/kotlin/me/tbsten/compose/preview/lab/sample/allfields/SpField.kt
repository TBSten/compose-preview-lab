package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.SpField
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@ComposePreviewLabOption(id = "SpFieldExample")
@Composable
internal fun SpFieldExample() = PreviewLab {
    Text(
        text = "Sample Text",
        fontSize = fieldValue { SpField("Font Size", 16.sp) },
    )
}
