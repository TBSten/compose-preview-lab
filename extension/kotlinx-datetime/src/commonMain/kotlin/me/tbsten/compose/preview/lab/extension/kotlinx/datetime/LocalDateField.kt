package me.tbsten.compose.preview.lab.extension.kotlinx.datetime

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlinx.serialization.KSerializer
import me.tbsten.compose.preview.lab.MutablePreviewLabField
import me.tbsten.compose.preview.lab.extension.kotlinx.datetime.component.LocalDateEditor

public class LocalDateField(label: String, initialValue: LocalDate) :
    MutablePreviewLabField<LocalDate>(
        label = label,
        initialValue = initialValue,
    ) {

    override fun valueCode(): String = "LocalDate(" +
        "year = ${value.year}, " +
        "month = ${value.month.number}, " +
        "day = ${value.day}" +
        ")"

    override fun serializer(): KSerializer<LocalDate> = LocalDate.serializer()

    @Composable
    override fun Content() {
        LocalDateEditor(
            date = value,
            onDateChange = { value = it },
        )
    }
}
