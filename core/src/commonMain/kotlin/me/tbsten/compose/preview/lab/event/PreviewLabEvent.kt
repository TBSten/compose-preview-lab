package me.tbsten.compose.preview.lab.event

import androidx.compose.runtime.Immutable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Represents an event that occurred during preview interaction
 * 
 * Captures user interactions and system events within the preview environment
 * for debugging and analysis purposes. Events are displayed in the Events inspector
 * tab with timestamps for tracking interaction sequences.
 * 
 * ```kotlin
 * // Events are typically created through PreviewLabScope
 * PreviewLab {
 *     Button(
 *         onClick = { onEvent("Button clicked", "User tapped the submit button") }
 *     ) {
 *         Text("Submit")
 *     }
 * }
 * 
 * // Events automatically include timestamp
 * val event = PreviewLabEvent(
 *     title = "Form submitted",
 *     description = "User completed the registration form"
 * )
 * ```
 * 
 * @param title Brief event description displayed in the events list
 * @param description Detailed event information (optional)
 * @param createAt Timestamp when the event occurred (auto-generated)
 * @see PreviewLabScope.onEvent
 */
@Immutable
@OptIn(ExperimentalTime::class)
internal data class PreviewLabEvent(
    val title: String,
    val description: String? = null,
    val createAt: Instant = Clock.System.now(),
)
