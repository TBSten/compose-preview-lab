@file:OptIn(ExperimentalTime::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.field.InstantField
import me.tbsten.compose.preview.lab.previewlab.PreviewLab

@Preview
@ComposePreviewLabOption(id = "InstantFieldExample")
@Composable
internal fun InstantFieldExample() = PreviewLab {
    Text(
        text = "Create at: ${
            fieldValue { InstantField("createAt", Clock.System.now()) }
        }",
    )
}
