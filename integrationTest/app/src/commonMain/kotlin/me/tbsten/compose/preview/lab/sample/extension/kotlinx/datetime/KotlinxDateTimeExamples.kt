package me.tbsten.compose.preview.lab.sample.extension.kotlinx.datetime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.extension.kotlinx.datetime.DatePeriodField
import me.tbsten.compose.preview.lab.extension.kotlinx.datetime.DateTimePeriodField
import me.tbsten.compose.preview.lab.extension.kotlinx.datetime.DayOfWeekField
import me.tbsten.compose.preview.lab.extension.kotlinx.datetime.LocalDateField
import me.tbsten.compose.preview.lab.extension.kotlinx.datetime.LocalDateTimeField
import me.tbsten.compose.preview.lab.extension.kotlinx.datetime.LocalTimeField
import me.tbsten.compose.preview.lab.extension.kotlinx.datetime.MonthField
import me.tbsten.compose.preview.lab.extension.kotlinx.datetime.TimeZoneField
import me.tbsten.compose.preview.lab.extension.kotlinx.datetime.withMainTimeZonesHint
import me.tbsten.compose.preview.lab.previewlab.PreviewLab

@Preview
@Composable
private fun KotlinxDateTimeExamples() = PreviewLab {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(20.dp),
    ) {
        Text(
            "createAt: ${
                fieldValue {
                    LocalDateTimeField(
                        label = "createAt",
                        initialValue = LocalDateTime(2000, 1, 1, 0, 0, 0, 0),
                    )
                }
            }",
        )

        Text(
            "birthday: ${
                fieldValue {
                    LocalDateField(
                        label = "birthday",
                        initialValue = LocalDate(2000, 1, 1),
                    )
                }
            }",
        )

        Text(
            "meetingTime: ${
                fieldValue {
                    LocalTimeField(
                        label = "meetingTime",
                        initialValue = LocalTime(15, 0, 0, 0),
                    )
                }
            }",
        )

        Text(
            "timeZone: ${
                fieldValue {
                    TimeZoneField(
                        label = "timeZone",
                        initialValue = TimeZone.UTC,
                    ).withMainTimeZonesHint()
                }
            }",
        )

        Text(
            "month: ${
                fieldValue {
                    MonthField(
                        label = "month",
                        initialValue = Month.APRIL,
                    )
                }
            }",
        )

        Text(
            "regularClosingDay: ${
                fieldValue {
                    DayOfWeekField(
                        label = "regularClosingDay",
                        initialValue = DayOfWeek.SUNDAY,
                    )
                }
            }",
        )

        Text(
            "numberOfDaysAchieved: ${
                fieldValue {
                    DatePeriodField(
                        label = "numberOfDaysAchieved",
                        initialValue = DatePeriod(days = 3),
                    )
                }
            }",
        )

        Text(
            "gameCompletionTime: ${
                fieldValue {
                    DateTimePeriodField(
                        label = "gameCompletionTime",
                        initialValue = DateTimePeriod(hours = 3),
                    )
                }
            }",
        )
    }
}

// Individual Field Examples for Documentation

@ComposePreviewLabOption(id = "LocalDateTimeFieldExample")
@Preview
@Composable
private fun LocalDateTimeFieldExample() = PreviewLab {
    val createdAt = fieldValue {
        LocalDateTimeField(
            label = "createdAt",
            initialValue = LocalDateTime(2024, 1, 1, 12, 0, 0, 0),
        )
    }
    Text("Created at: $createdAt", modifier = Modifier.padding(16.dp))
}

@ComposePreviewLabOption(id = "LocalDateFieldExample")
@Preview
@Composable
private fun LocalDateFieldExample() = PreviewLab {
    val birthday = fieldValue {
        LocalDateField(
            label = "birthday",
            initialValue = LocalDate(2000, 1, 1),
        )
    }
    Text("Birthday: $birthday", modifier = Modifier.padding(16.dp))
}

@ComposePreviewLabOption(id = "LocalTimeFieldExample")
@Preview
@Composable
private fun LocalTimeFieldExample() = PreviewLab {
    val meetingTime = fieldValue {
        LocalTimeField(
            label = "meetingTime",
            initialValue = LocalTime(15, 0, 0, 0),
        )
    }
    Text("Meeting time: $meetingTime", modifier = Modifier.padding(16.dp))
}

@ComposePreviewLabOption(id = "TimeZoneFieldExample")
@Preview
@Composable
private fun TimeZoneFieldExample() = PreviewLab {
    val timeZone = fieldValue {
        TimeZoneField(
            label = "timeZone",
            initialValue = TimeZone.UTC,
        ).withMainTimeZonesHint()
    }
    Text("TimeZone: $timeZone", modifier = Modifier.padding(16.dp))
}

@ComposePreviewLabOption(id = "MonthFieldExample")
@Preview
@Composable
private fun MonthFieldExample() = PreviewLab {
    val month = fieldValue {
        MonthField(
            label = "month",
            initialValue = Month.APRIL,
        )
    }
    Text("Month: $month", modifier = Modifier.padding(16.dp))
}

@ComposePreviewLabOption(id = "DayOfWeekFieldExample")
@Preview
@Composable
private fun DayOfWeekFieldExample() = PreviewLab {
    val dayOfWeek = fieldValue {
        DayOfWeekField(
            label = "regularClosingDay",
            initialValue = DayOfWeek.SUNDAY,
        )
    }
    Text("Regular closing day: $dayOfWeek", modifier = Modifier.padding(16.dp))
}

@ComposePreviewLabOption(id = "DatePeriodFieldExample")
@Preview
@Composable
private fun DatePeriodFieldExample() = PreviewLab {
    val datePeriod = fieldValue {
        DatePeriodField(
            label = "subscriptionPeriod",
            initialValue = DatePeriod(months = 1),
        )
    }
    Text("Subscription period: $datePeriod", modifier = Modifier.padding(16.dp))
}

@ComposePreviewLabOption(id = "DateTimePeriodFieldExample")
@Preview
@Composable
private fun DateTimePeriodFieldExample() = PreviewLab {
    val dateTimePeriod = fieldValue {
        DateTimePeriodField(
            label = "duration",
            initialValue = DateTimePeriod(hours = 2, minutes = 30),
        )
    }
    Text("Duration: $dateTimePeriod", modifier = Modifier.padding(16.dp))
}
