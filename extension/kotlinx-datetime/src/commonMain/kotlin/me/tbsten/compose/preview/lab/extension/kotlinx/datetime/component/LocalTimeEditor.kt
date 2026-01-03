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
import kotlinx.datetime.LocalTime
import me.tbsten.compose.preview.lab.extension.kotlinx.datetime.util.with
import me.tbsten.compose.preview.lab.field.component.TextFieldContent
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Text

@Composable
internal fun LocalTimeEditor(
    hour: Int,
    minute: Int,
    second: Double,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onSecondChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        TextFieldContent<Int>(
            label = "hour",
            value = hour,
            onValueChange = onHourChange,
            toString = { it.toString() },
            toValue = { runCatching { it.toInt() } },
            textStyle = PreviewLabTheme.typography.input.copy(textAlign = TextAlign.Center),
            modifier = Modifier.weight(1f),
        )

        Text(":")

        TextFieldContent<Int>(
            label = "minute",
            value = minute,
            onValueChange = onMinuteChange,
            toString = { it.toString() },
            toValue = { runCatching { it.toInt() } },
            textStyle = PreviewLabTheme.typography.input.copy(textAlign = TextAlign.Center),
            modifier = Modifier.weight(1f),
        )

        Text(":")

        TextFieldContent<Double>(
            label = "day",
            value = second,
            onValueChange = onSecondChange,
            toString = { it.toString() },
            toValue = { runCatching { it.toDouble() } },
            textStyle = PreviewLabTheme.typography.input.copy(textAlign = TextAlign.Center),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun LocalTimeEditor(time: LocalTime, onTimeChange: (LocalTime) -> Unit, modifier: Modifier = Modifier) {
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column {
        LocalTimeEditor(
            hour = time.hour,
            minute = time.minute,
            second = time.second + time.nanosecond * 1_000_000_000.0,
            onHourChange = {
                runCatching { time.with(hour = it) }
                    .onSuccess(onTimeChange)
                    .also { errorMessage = it.exceptionOrNull()?.message }
            },
            onMinuteChange = {
                runCatching { time.with(minute = it) }
                    .onSuccess(onTimeChange)
                    .also { errorMessage = it.exceptionOrNull()?.message }
            },
            onSecondChange = {
                runCatching {
                    time.with(
                        second = it.toInt(),
                        nanosecond = ((it - it.toInt()) * 1_000_000_000).toInt(),
                    )
                }
                    .onSuccess(onTimeChange)
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
