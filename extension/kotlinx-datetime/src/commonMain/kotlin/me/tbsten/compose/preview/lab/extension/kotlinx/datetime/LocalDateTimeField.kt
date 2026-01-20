package me.tbsten.compose.preview.lab.extension.kotlinx.datetime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number
import kotlinx.serialization.KSerializer
import me.tbsten.compose.preview.lab.MutablePreviewLabField
import me.tbsten.compose.preview.lab.extension.kotlinx.datetime.component.LocalDateEditor
import me.tbsten.compose.preview.lab.extension.kotlinx.datetime.component.LocalTimeEditor
import me.tbsten.compose.preview.lab.extension.kotlinx.datetime.util.with
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Text

public class LocalDateTimeField(label: String, initialValue: LocalDateTime) :
    MutablePreviewLabField<LocalDateTime>(
        label = label,
        initialValue = initialValue,
    ) {

    override fun valueCode(): String = "LocalDateTime(" +
        "year = ${value.year}, " +
        "month = ${value.month.number}, " +
        "day = ${value.day}, " +
        "hour = ${value.hour}, " +
        "minute = ${value.minute}, " +
        "second = ${value.second}, " +
        "nanosecond = ${value.nanosecond}" +
        ")"

    override fun serializer(): KSerializer<LocalDateTime> = LocalDateTime.serializer()

    @Composable
    override fun Content() {
        var errorMessage by remember { mutableStateOf<String?>(null) }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LocalDateEditor(
                date = value.date,
                onDateChange = {
                    runCatching { value.with(date = it) }
                        .onSuccess { value = it }
                        .also { errorMessage = it.exceptionOrNull()?.message }
                },
            )

            LocalTimeEditor(
                time = value.time,
                onTimeChange = {
                    runCatching { value.with(time = it) }
                        .onSuccess { value = it }
                        .also { errorMessage = it.exceptionOrNull()?.message }
                },
            )

            errorMessage?.let { error ->
                Text(
                    text = error,
                    style = PreviewLabTheme.typography.body2,
                    color = PreviewLabTheme.colors.error,
                )
            }
        }
    }
}
