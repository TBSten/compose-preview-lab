package me.tbsten.compose.preview.lab.ui.util

import androidx.compose.ui.Modifier
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi

@InternalComposePreviewLabApi
public inline fun Modifier.thenIf(condition: Boolean, body: Modifier.() -> Modifier): Modifier = then(
    if (condition) {
        Modifier.body()
    } else {
        Modifier
    },
)

@InternalComposePreviewLabApi
public inline fun <Value> Modifier.thenIfNotNull(value: Value?, body: Modifier.(Value) -> Modifier): Modifier = then(
    if (value != null) {
        Modifier.body(value)
    } else {
        Modifier
    },
)
