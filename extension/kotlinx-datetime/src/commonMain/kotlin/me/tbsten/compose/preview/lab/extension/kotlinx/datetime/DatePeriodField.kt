package me.tbsten.compose.preview.lab.extension.kotlinx.datetime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DatePeriod
import me.tbsten.compose.preview.lab.MutablePreviewLabField
import me.tbsten.compose.preview.lab.extension.kotlinx.datetime.component.LocalDateEditor
import me.tbsten.compose.preview.lab.extension.kotlinx.datetime.util.with
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.PreviewLabText

class DatePeriodField(label: String, initialValue: DatePeriod) :
    MutablePreviewLabField<DatePeriod>(
        label = label,
        initialValue = initialValue,
    ) {
    override fun valueCode(): String = "DatePeriod(" +
        "years = ${value.years}, " +
        "months = ${value.months}, " +
        "days = ${value.days}" +
        ")"

    override fun serializer() = DatePeriod.serializer()

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
