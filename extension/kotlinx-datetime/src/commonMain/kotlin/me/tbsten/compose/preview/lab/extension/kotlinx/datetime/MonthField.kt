package me.tbsten.compose.preview.lab.extension.kotlinx.datetime

import kotlinx.datetime.Month
import me.tbsten.compose.preview.lab.field.EnumField
import me.tbsten.compose.preview.lab.field.WithValueCodeField

@Suppress("ktlint:standard:function-naming", "FunctionName")
public fun MonthField(label: String, initialValue: Month): WithValueCodeField<Month> = EnumField<Month>(
    label = label,
    initialValue = initialValue,
)
