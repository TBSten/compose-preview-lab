package me.tbsten.compose.preview.lab.extension.kotlinx.datetime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DateTimePeriod
import me.tbsten.compose.preview.lab.MutablePreviewLabField
import me.tbsten.compose.preview.lab.extension.kotlinx.datetime.component.LocalDateEditor
import me.tbsten.compose.preview.lab.extension.kotlinx.datetime.component.LocalTimeEditor
import me.tbsten.compose.preview.lab.extension.kotlinx.datetime.util.with
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.PreviewLabText

class DateTimePeriodField(label: String, initialValue: DateTimePeriod) :
    MutablePreviewLabField<DateTimePeriod>(
        label = label,
        initialValue = initialValue,
    ) {
    override fun valueCode(): String = "DateTimePeriod(" +
        "years = ${value.years}, " +
        "months = ${value.months}, " +
        "days = ${value.days}, " +
        "hours = ${value.hours}, " +
        "minutes = ${value.minutes}, " +
        "seconds = ${value.seconds}, " +
        "nanoseconds = ${value.nanoseconds}" +
        ")"

    override fun serializer() = DateTimePeriod.serializer()

    @Composable
    override fun Content() {
        var errorMessage by remember { mutableStateOf<String?>(null) }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LocalDateEditor(
                year = value.years,
                onYearChange = {
                    runCatching { value.with(years = it) }
                        .onSuccess { value = it }
                        .also { errorMessage = it.exceptionOrNull()?.message }
                },
                month = value.months,
                onMonthChange = {
                    runCatching { value.with(months = it) }
                        .onSuccess { value = it }
                        .also { errorMessage = it.exceptionOrNull()?.message }
                },
                day = value.days,
                onDayChange = {
                    runCatching { value.with(days = it) }
                        .onSuccess { value = it }
                        .also { errorMessage = it.exceptionOrNull()?.message }
                },
            )

            LocalTimeEditor(
                hour = value.hours,
                onHourChange = {
                    runCatching { value.with(hours = it) }
                        .onSuccess { value = it }
                        .also { errorMessage = it.exceptionOrNull()?.message }
                },
                minute = value.minutes,
                onMinuteChange = {
                    runCatching { value.with(minutes = it) }
                        .onSuccess { value = it }
                        .also { errorMessage = it.exceptionOrNull()?.message }
                },
                second = value.seconds + value.nanoseconds / 1_000_000_000.0,
                onSecondChange = {
                    runCatching {
                        value.with(
                            seconds = it.toInt(),
                            nanoseconds = ((it - it.toInt()) * 1_000_000_000).toLong(),
                        )
                    }
                        .onSuccess { value = it }
                        .also { errorMessage = it.exceptionOrNull()?.message }
                },
            )

            errorMessage?.let { error ->
                PreviewLabText(
                    text = error,
                    style = PreviewLabTheme.typography.body2,
                    color = PreviewLabTheme.colors.error,
                )
            }
        }
    }
}
