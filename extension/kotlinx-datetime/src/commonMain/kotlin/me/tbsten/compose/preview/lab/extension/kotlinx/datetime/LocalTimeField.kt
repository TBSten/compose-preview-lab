package me.tbsten.compose.preview.lab.extension.kotlinx.datetime

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalTime
import me.tbsten.compose.preview.lab.MutablePreviewLabField
import me.tbsten.compose.preview.lab.extension.kotlinx.datetime.component.LocalTimeEditor

class LocalTimeField(label: String, initialValue: LocalTime) :
    MutablePreviewLabField<LocalTime>(
        label = label,
        initialValue = initialValue,
    ) {

    override fun valueCode(): String = "LocalTime(" +
        "hour = ${value.hour}, " +
        "minute = ${value.minute}, " +
        "second = ${value.second}, " +
        "nanosecond = ${value.nanosecond}" +
        ")"

    override fun serializer() = LocalTime.serializer()

    @Composable
    override fun Content() {
        LocalTimeEditor(
            time = value,
            onTimeChange = { value = it },
        )
    }
}
