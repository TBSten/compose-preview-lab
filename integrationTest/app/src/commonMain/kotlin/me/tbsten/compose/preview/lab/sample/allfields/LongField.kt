package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.LongField
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@ComposePreviewLabOption(id = "LongFieldExample")
@Composable
internal fun LongFieldExample() = PreviewLab {
    DateDisplay(
        timestamp = fieldValue { LongField("Timestamp", 0L) },
    )
}

@Composable
internal fun DateDisplay(timestamp: Long) {
    Text("Timestamp: $timestamp")
}
