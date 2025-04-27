package me.tbsten.compose.preview.lab.me

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import me.tbsten.compose.preview.lab.me.field.PreviewLabField

class PreviewLabScope internal constructor() {
    internal val fields = mutableListOf<PreviewLabField<*>>()

    @Composable
    fun <Value> field(builder: () -> PreviewLabField<Value>): MutableState<Value> {
        val field = remember { builder() }
        DisposableEffect(field) {
            fields.add(field)
            onDispose { fields.remove(field) }
        }
        return field
    }

    @Composable
    fun <Value> fieldValue(builder: () -> PreviewLabField<Value>) =
        field(builder).value
}
