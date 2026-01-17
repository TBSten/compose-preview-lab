package me.tbsten.compose.preview.lab.extension.kotlinx.datetime

import kotlinx.datetime.DayOfWeek
import me.tbsten.compose.preview.lab.field.EnumField

@Suppress("ktlint:standard:function-naming", "FunctionName")
fun DayOfWeekField(label: String, initialValue: DayOfWeek) = EnumField<DayOfWeek>(
    label = label,
    initialValue = initialValue,
)
