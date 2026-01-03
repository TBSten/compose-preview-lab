package me.tbsten.compose.preview.lab.extension.kotlinx.datetime

import kotlinx.datetime.Month
import me.tbsten.compose.preview.lab.field.EnumField

@Suppress("ktlint:standard:function-naming", "FunctionName")
fun MonthField(label: String, initialValue: Month) = EnumField<Month>(
    label = label,
    initialValue = initialValue,
)
