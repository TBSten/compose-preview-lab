package me.tbsten.compose.preview.lab.extension.kotlinx.datetime.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import me.tbsten.compose.preview.lab.extension.kotlinx.datetime.util.with
import me.tbsten.compose.preview.lab.field.component.TextFieldContent
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Text

@Composable
internal fun LocalDateEditor(
    year: Int,
    month: Int,
    day: Int,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onDayChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        TextFieldContent<Int>(
            label = "year",
            value = year,
            onValueChange = onYearChange,
            toString = { it.toString() },
            toValue = { runCatching { it.toInt() } },
            textStyle = PreviewLabTheme.typography.input.copy(textAlign = TextAlign.Center),
            modifier = Modifier.weight(1f),
        )

        Text("/")

        TextFieldContent<Int>(
            label = "number",
            value = month,
            onValueChange = onMonthChange,
            toString = { it.toString() },
            toValue = { runCatching { it.toInt() } },
            textStyle = PreviewLabTheme.typography.input.copy(textAlign = TextAlign.Center),
            modifier = Modifier.weight(1f),
        )

        Text("/")

        TextFieldContent<Int>(
            label = "day",
            value = day,
            onValueChange = onDayChange,
            toString = { it.toString() },
            toValue = { runCatching { it.toInt() } },
            textStyle = PreviewLabTheme.typography.input.copy(textAlign = TextAlign.Center),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun LocalDateEditor(date: LocalDate, onDateChange: (LocalDate) -> Unit, modifier: Modifier = Modifier) {
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier,
    ) {
        LocalDateEditor(
            year = date.year,
            onYearChange = {
                runCatching { date.with(year = it) }
                    .onSuccess(onDateChange)
                    .also { errorMessage = it.exceptionOrNull()?.message }
            },
            month = date.month.number,
            onMonthChange = {
                runCatching { date.with(month = it) }
                    .onSuccess(onDateChange)
                    .also { errorMessage = it.exceptionOrNull()?.message }
            },
            day = date.day,
            onDayChange = {
                runCatching { date.with(day = it) }
                    .onSuccess(onDateChange)
                    .also { errorMessage = it.exceptionOrNull()?.message }
            },
        )

        errorMessage?.let { errorMessage ->
            Text(
                text = errorMessage,
                style = PreviewLabTheme.typography.body2,
                color = PreviewLabTheme.colors.error,
            )
        }
    }
}
