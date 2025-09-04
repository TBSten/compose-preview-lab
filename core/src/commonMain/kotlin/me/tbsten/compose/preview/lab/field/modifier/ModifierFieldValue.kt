package me.tbsten.compose.preview.lab.field.modifier

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface ModifierFieldValue {
    fun Modifier.createModifier(): Modifier

    @Composable
    fun Builder()

    fun then(other: ModifierFieldValue): ModifierFieldValueList = ModifierFieldValueList(this, other)

    companion object : ModifierFieldValueList()
}

internal fun createModifierFrom(value: ModifierFieldValue) = Modifier.then(with(value) { Modifier.createModifier() })

open class ModifierFieldValueList(private val values: List<ModifierFieldValue>) : List<ModifierFieldValue> by values {
    constructor(vararg values: ModifierFieldValue) : this(values = values.toList())

    fun createModifier(): Modifier = values.fold<ModifierFieldValue, Modifier>(Modifier) { modifier, value ->
        modifier.then(createModifierFrom(value))
    }

    fun then(other: ModifierFieldValue): ModifierFieldValueList = ModifierFieldValueList(
        this.values + other,
    )

    fun then(other: ModifierFieldValueList): ModifierFieldValueList = ModifierFieldValueList(
        this.values + other.values,
    )

    companion object : ModifierFieldValueList()
}
