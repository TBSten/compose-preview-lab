package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.DpSizeField
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@ComposePreviewLabOption(id = "DpSizeFieldExample")
@Composable
internal fun DpSizeFieldExample() = PreviewLab {
    val buttonSize = fieldValue { DpSizeField("Button Size", DpSize(120.dp, 48.dp)) }

    Button(
        onClick = { },
        modifier = Modifier.size(buttonSize)
    ) {
        Text("Sized Button")
    }
}
