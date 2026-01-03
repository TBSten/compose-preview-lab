---
title: kotlinx-datetime Extension
sidebar_position: 1
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';
import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';

# kotlinx-datetime Extension

`extension-kotlinx-datetime` モジュールは、[kotlinx-datetime](https://github.com/Kotlin/kotlinx-datetime) ライブラリの型に対応した Field を提供します。

## インストール

`starter` モジュールには含まれていないため、個別に依存関係を追加する必要があります。

<Tabs>
  <TabItem value="compose-multiplatform" label="Compose Multiplatform" default>

```kotlin title="build.gradle.kts"
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose Preview Lab
                implementation("me.tbsten.compose.preview.lab:starter:<version>")

                // highlight-next-line
                // kotlinx-datetime Extension
                // highlight-next-line
                implementation("me.tbsten.compose.preview.lab:extension-kotlinx-datetime:<version>")

                // kotlinx-datetime 本体も必要
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:<kotlinx-datetime-version>")
            }
        }
    }
}
```

  </TabItem>
  <TabItem value="android" label="Android">

```kotlin title="app/build.gradle.kts"
dependencies {
    // Compose Preview Lab
    implementation("me.tbsten.compose.preview.lab:starter:<version>")

    // highlight-next-line
    // kotlinx-datetime Extension
    // highlight-next-line
    implementation("me.tbsten.compose.preview.lab:extension-kotlinx-datetime:<version>")

    // kotlinx-datetime 本体も必要
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:<kotlinx-datetime-version>")
}
```

  </TabItem>
</Tabs>

## 利用可能な Field

| Field | 対応する kotlinx-datetime の型 | 説明 |
|-------|-------------------------------|------|
| `LocalDateTimeField` | `LocalDateTime` | 日付と時刻を編集 |
| `LocalDateField` | `LocalDate` | 日付を編集 |
| `LocalTimeField` | `LocalTime` | 時刻を編集 |
| `TimeZoneField` | `TimeZone` | タイムゾーンを編集 |
| `MonthField` | `Month` | 月を選択 |
| `DayOfWeekField` | `DayOfWeek` | 曜日を選択 |
| `DatePeriodField` | `DatePeriod` | 日付の期間を編集 |
| `DateTimePeriodField` | `DateTimePeriod` | 日時の期間を編集 |

## 使用例

### LocalDateTimeField

日付と時刻を編集できる Field です。

```kotlin
PreviewLab {
    val createdAt = fieldValue {
        // highlight-next-line
        LocalDateTimeField(
            label = "createdAt",
            initialValue = LocalDateTime(2024, 1, 1, 12, 0, 0, 0),
        )
    }
    Text("Created at: $createdAt")
}
```

<EmbeddedPreviewLab
 previewId="LocalDateTimeFieldExample"
 title="LocalDateTimeField Example"
/>

### LocalDateField

日付を編集できる Field です。

```kotlin
PreviewLab {
    val birthday = fieldValue {
        // highlight-next-line
        LocalDateField(
            label = "birthday",
            initialValue = LocalDate(2000, 1, 1),
        )
    }
    Text("Birthday: $birthday")
}
```

<EmbeddedPreviewLab
 previewId="LocalDateFieldExample"
 title="LocalDateField Example"
/>

### LocalTimeField

時刻を編集できる Field です。

```kotlin
PreviewLab {
    val meetingTime = fieldValue {
        // highlight-next-line
        LocalTimeField(
            label = "meetingTime",
            initialValue = LocalTime(15, 0, 0, 0),
        )
    }
    Text("Meeting time: $meetingTime")
}
```

<EmbeddedPreviewLab
 previewId="LocalTimeFieldExample"
 title="LocalTimeField Example"
/>

### TimeZoneField

タイムゾーンを編集できる Field です。`withMainTimeZonesHint()` や `withAllTimeZonesHint()` を使用して、よく使用されるタイムゾーンをヒントとして表示できます。

```kotlin
PreviewLab {
    val timeZone = fieldValue {
        TimeZoneField(
            label = "timeZone",
            initialValue = TimeZone.UTC,
        // highlight-next-line
        ).withMainTimeZonesHint() // 主要なタイムゾーンをヒントとして表示
    }
    Text("TimeZone: $timeZone")
}
```

<EmbeddedPreviewLab
 previewId="TimeZoneFieldExample"
 title="TimeZoneField Example"
/>

**ヒント関数:**

- `withMainTimeZonesHint()` - 主要なタイムゾーン（UTC, JST, EST など）をヒントとして表示
- `withAllTimeZonesHint()` - すべての利用可能なタイムゾーンをヒントとして表示

### MonthField

月を選択できる Field です。内部的には `EnumField` を使用しています。

```kotlin
PreviewLab {
    val month = fieldValue {
        // highlight-next-line
        MonthField(
            label = "month",
            initialValue = Month.APRIL,
        )
    }
    Text("Month: $month")
}
```

<EmbeddedPreviewLab
 previewId="MonthFieldExample"
 title="MonthField Example"
/>

### DayOfWeekField

曜日を選択できる Field です。内部的には `EnumField` を使用しています。

```kotlin
PreviewLab {
    val dayOfWeek = fieldValue {
        // highlight-next-line
        DayOfWeekField(
            label = "regularClosingDay",
            initialValue = DayOfWeek.SUNDAY,
        )
    }
    Text("Regular closing day: $dayOfWeek")
}
```

<EmbeddedPreviewLab
 previewId="DayOfWeekFieldExample"
 title="DayOfWeekField Example"
/>

### DatePeriodField

日付の期間を編集できる Field です。年・月・日の期間を表します。

```kotlin
PreviewLab {
    val datePeriod = fieldValue {
        // highlight-next-line
        DatePeriodField(
            label = "subscriptionPeriod",
            initialValue = DatePeriod(months = 1),
        )
    }
    Text("Subscription period: $datePeriod")
}
```

<EmbeddedPreviewLab
 previewId="DatePeriodFieldExample"
 title="DatePeriodField Example"
/>

### DateTimePeriodField

日時の期間を編集できる Field です。年・月・日・時・分・秒・ナノ秒の期間を表します。

```kotlin
PreviewLab {
    val dateTimePeriod = fieldValue {
        // highlight-next-line
        DateTimePeriodField(
            label = "duration",
            initialValue = DateTimePeriod(hours = 2, minutes = 30),
        )
    }
    Text("Duration: $dateTimePeriod")
}
```

<EmbeddedPreviewLab
 previewId="DateTimePeriodFieldExample"
 title="DateTimePeriodField Example"
/>

## valueCode() のサポート

すべての Field は `valueCode()` をサポートしており、Inspector の Code タブで適切な Kotlin コードが生成されます。

例：
- `LocalDateTimeField` → `LocalDateTime(year = 2024, month = 1, day = 1, hour = 12, minute = 0, second = 0, nanosecond = 0)`
- `TimeZoneField` → `TimeZone.of("Asia/Tokyo")`
- `MonthField` → `Month.APRIL`

## 関連リンク

- [Fields Overview](../02-fields/01-overview.md) - Field の基本的な使い方
- [All Fields](../02-fields/02-all-fields.md) - ビルトインの Field 一覧
- [kotlinx-datetime GitHub](https://github.com/Kotlin/kotlinx-datetime) - kotlinx-datetime ライブラリ
