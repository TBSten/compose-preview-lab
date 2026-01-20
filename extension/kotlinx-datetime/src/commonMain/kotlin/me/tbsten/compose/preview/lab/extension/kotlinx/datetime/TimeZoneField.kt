package me.tbsten.compose.preview.lab.extension.kotlinx.datetime

import androidx.compose.runtime.Composable
import kotlinx.datetime.TimeZone
import kotlinx.serialization.KSerializer
import me.tbsten.compose.preview.lab.MutablePreviewLabField
import me.tbsten.compose.preview.lab.field.component.TextFieldContent
import me.tbsten.compose.preview.lab.field.withHint

public class TimeZoneField(label: String, initialValue: TimeZone) :
    MutablePreviewLabField<TimeZone>(
        label = label,
        initialValue = initialValue,
    ) {

    override fun valueCode(): String = "TimeZone.of(\"${value}\")"

    override fun serializer(): KSerializer<TimeZone> = TimeZone.serializer()

    @Composable
    override fun Content() {
        TextFieldContent(
            toString = { it.id },
            toValue = { runCatching { TimeZone.of(zoneId = it) } },
        )
    }
}

public fun MutablePreviewLabField<TimeZone>.withMainTimeZonesHint(): MutablePreviewLabField<TimeZone> = withHint(
    *mainTimeZones.mapNotNull { (label, id) ->
        runCatching { TimeZone.of(zoneId = id) }
            .getOrNull()
            ?.let { label to it }
    }.toTypedArray(),
)

public fun MutablePreviewLabField<TimeZone>.withAllTimeZonesHint(): MutablePreviewLabField<TimeZone> = withHint(
    *allTimeZones.mapNotNull { (label, id) ->
        runCatching { TimeZone.of(zoneId = id) }
            .getOrNull()
            ?.let { label to it }
    }.toTypedArray(),
)
