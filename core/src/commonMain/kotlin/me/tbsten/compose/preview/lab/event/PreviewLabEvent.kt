package me.tbsten.compose.preview.lab.event

import androidx.compose.runtime.Immutable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Immutable
@OptIn(ExperimentalTime::class)
internal data class PreviewLabEvent(
    val title: String,
    val description: String? = null,
    val createAt: Instant = Clock.System.now(),
)
