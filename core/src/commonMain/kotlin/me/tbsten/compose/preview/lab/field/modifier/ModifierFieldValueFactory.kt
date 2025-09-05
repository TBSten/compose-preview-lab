package me.tbsten.compose.preview.lab.field.modifier

import androidx.compose.runtime.Composable

interface ModifierFieldValueFactory<V : ModifierFieldValue> {
    val title: String
    val canCreate: Boolean get() = false

    @Composable
    fun Content(createButton: @Composable () -> Unit)

    fun create(): Result<V>
}

typealias ModifierFieldValueFactories = List<ModifierFieldValueFactory<*>>
