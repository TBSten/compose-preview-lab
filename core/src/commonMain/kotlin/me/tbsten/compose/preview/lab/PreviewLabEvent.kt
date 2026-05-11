package me.tbsten.compose.preview.lab

import androidx.compose.runtime.Immutable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Event surfaced in the Events inspector tab — typically emitted through
 * [PreviewLabScope.onEvent][me.tbsten.compose.preview.lab.previewlab.PreviewLabScope.onEvent]
 * rather than constructed directly. The default [createAt] timestamps the entry at creation.
 *
 * ```kotlin
 * PreviewLab {
 *     Button(onClick = { onEvent("Button clicked", "User tapped the submit button") }) {
 *         Text("Submit")
 *     }
 * }
 * ```
 *
 * @see me.tbsten.compose.preview.lab.previewlab.PreviewLabScope.onEvent
 */
@Immutable
@OptIn(ExperimentalTime::class)
@ExperimentalComposePreviewLabApi
data class PreviewLabEvent(val title: String, val description: String? = null, val createAt: Instant = Clock.System.now())
