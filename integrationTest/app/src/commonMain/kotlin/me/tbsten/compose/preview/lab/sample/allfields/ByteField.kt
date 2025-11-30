package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.ByteField
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@ComposePreviewLabOption(id = "ByteFieldExample")
@Composable
internal fun ByteFieldExample() = PreviewLab {
    FlagDisplay(
        flag = fieldValue { ByteField("Flag", 0) },
    )
}

@Composable
internal fun FlagDisplay(flag: Byte) {
    Text("Flag: $flag")
}
