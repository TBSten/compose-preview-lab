package me.tbsten.compose.preview.lab.util

import androidx.compose.ui.Modifier

internal fun Modifier.thenIf(condition: Boolean, body: Modifier.() -> Modifier): Modifier = then(
    if (condition) {
        Modifier.body()
    } else {
        Modifier
    },
)

internal fun <Value> Modifier.thenIfNotNull(value: Value?, body: Modifier.(Value) -> Modifier): Modifier = then(
    if (value != null) {
        Modifier.body(value)
    } else {
        Modifier
    },
)
