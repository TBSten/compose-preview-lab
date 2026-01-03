package me.tbsten.compose.preview.lab.extension.kotlinx.datetime.util

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.number

internal fun LocalDate.with(year: Int = this.year, month: Int = this.month.number, day: Int = this.day) = LocalDate(
    year = year,
    month = month,
    day = day,
)

internal fun LocalTime.with(
    hour: Int = this.hour,
    minute: Int = this.minute,
    second: Int = this.second,
    nanosecond: Int = this.nanosecond,
) = LocalTime(
    hour = hour,
    minute = minute,
    second = second,
    nanosecond = nanosecond,
)

internal fun LocalDateTime.with(date: LocalDate = this.date, time: LocalTime = this.time) = LocalDateTime(
    date = date,
    time = time,
)

internal fun DateTimePeriod.with(
    years: Int = this.years,
    months: Int = this.months,
    days: Int = this.days,
    hours: Int = this.hours,
    minutes: Int = this.minutes,
    seconds: Int = this.seconds,
    nanoseconds: Long = this.nanoseconds.toLong(),
) = DateTimePeriod(
    years = years,
    months = months,
    days = days,
    hours = hours,
    minutes = minutes,
    seconds = seconds,
    nanoseconds = nanoseconds,
)

internal fun DatePeriod.with(years: Int = this.years, months: Int = this.months, days: Int = this.days) = DatePeriod(
    years = years,
    months = months,
    days = days,
)
