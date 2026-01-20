package me.tbsten.compose.preview.lab.extension.kotlinx.datetime

import kotlinx.datetime.DayOfWeek
import me.tbsten.compose.preview.lab.field.EnumField
import me.tbsten.compose.preview.lab.field.WithValueCodeField

@Suppress("ktlint:standard:function-naming", "FunctionName")
public fun DayOfWeekField(label: String, initialValue: DayOfWeek): WithValueCodeField<DayOfWeek> = EnumField<DayOfWeek>(
    label = label,
    initialValue = initialValue,
)
