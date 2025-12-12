package me.tbsten.compose.preview.lab.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import me.tbsten.compose.preview.lab.previewlab.PreviewLabScope

/**
 * An effect that calls onEvent to record an event when a value changes.
 * This is useful when you want to monitor a specific value in a Composable, such as a CompositionLocal value.
 *
 * ```kt
 * PreviewLab {
 *   ChangeEvent(LocalDensity.current, "Density")
 * }
 * ```
 *
 * @param value Value at which changes are detected.
 * @param title title of the event to be passed to onEvent.
 * @param description description of the event to be passed to onEvent.
 *
 * @see PreviewLabScope.onEvent
 */
@Composable
fun <Value> PreviewLabScope.ChangeEvent(value: Value, title: String, description: String? = null) {
    LaunchedEffect(value) {
        onEvent(
            title = title,
            description = description,
        )
    }
}

/**
 * An effect that calls onEvent to record an event when a value changes.
 * This is useful when you want to monitor a specific value in a Composable, such as a CompositionLocal value.
 *
 * ```kt
 * PreviewLab {
 *   ChangeEvent(LocalDensity.current, { "Density changed: $it" })
 * }
 * ```
 *
 * @param value Value at which changes are detected.
 * @param title title of the event to be passed to onEvent.
 * @param description description of the event to be passed to onEvent.
 *
 * @see PreviewLabScope.onEvent
 */
@Composable
fun <Value> PreviewLabScope.ChangeEvent(value: Value, title: (Value) -> String, description: (Value) -> String? = { null }) {
    LaunchedEffect(value) {
        onEvent(
            title = title(value),
            description = description(value),
        )
    }
}
