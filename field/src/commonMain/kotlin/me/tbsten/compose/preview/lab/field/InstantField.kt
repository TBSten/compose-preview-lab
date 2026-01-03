@file:OptIn(ExperimentalTime::class)

package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import me.tbsten.compose.preview.lab.MutablePreviewLabField
import me.tbsten.compose.preview.lab.field.component.TextFieldContent
import me.tbsten.compose.preview.lab.ui.components.Text

class InstantField(label: String, initialValue: Instant) :
    MutablePreviewLabField<Instant>(
        label = label,
        initialValue = initialValue,
    ) {
    override fun valueCode(): String = "Instant.fromEpochMilliseconds($value)"

    @Composable
    override fun Content() {
        TextFieldContent(
            toString = { it.toEpochMilliseconds().toString() },
            toValue = { runCatching { Instant.fromEpochMilliseconds(it.toLong()) } },
            placeholder = { Text("EpochMilliseconds") },
        )
    }
}
