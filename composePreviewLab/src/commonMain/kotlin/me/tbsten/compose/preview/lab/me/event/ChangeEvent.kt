package me.tbsten.compose.preview.lab.me.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import me.tbsten.compose.preview.lab.me.PreviewLabScope

@Composable
fun <Value> PreviewLabScope.ChangeEvent(
    value: Value,
    title: String,
    description: String? = null,
) {
    LaunchedEffect(value) {
        onEvent(
            title = title,
            description = description,
        )
    }
}

@Composable
fun <Value> PreviewLabScope.ChangeEvent(
    value: Value,
    title: (Value) -> String,
    description: (Value) -> String? = { null },
) {
    LaunchedEffect(value) {
        onEvent(
            title = title(value),
            description = description(value),
        )
    }
}
