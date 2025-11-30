package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.BooleanField
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@ComposePreviewLabOption(id = "BooleanFieldExample")
@Composable
internal fun BooleanFieldExample() = PreviewLab {
    BooleanFieldMyButton(
        enabled = fieldValue { BooleanField("enabled", true) },
    )
}

@Composable
internal fun BooleanFieldMyButton(enabled: Boolean) {
    Button(onClick = {}, enabled = enabled) {
        Text("Button")
    }
}
