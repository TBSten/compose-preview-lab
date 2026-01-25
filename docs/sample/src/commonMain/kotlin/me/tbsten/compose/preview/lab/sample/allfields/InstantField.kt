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

/**
 * Demonstrates [InstantField] for editing Kotlin time [kotlin.time.Instant] values.
 *
 * Edit the timestamp to test date/time-sensitive UI components.
 * Uses Kotlin's standard library Instant type (not kotlinx.datetime).
 */
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
