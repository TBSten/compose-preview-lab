package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

/**
 * Field class specified in PreviewLabScope.field/fieldValue.
 * In reality, it may be better to use the value property, getValue method, and setValue method to implement MutableState.
 *
 * In many cases, it is sufficient to use StringField, SelectableField, etc., and it is unlikely that this class will be set directly.
 * You would use this Composable if you want to create a Field of a custom type (set the value type parameter to your custom type) or if you want to create a UI for a custom Field (override the View method).
 *
 * @property label The label for this field. This is not used in any of the program logic, but only for display purposes, so it is best to set it in a language that is easy for your team members to read.
 * @property initialValue Default value for this field.
 */
interface PreviewLabField<Value> {
    val label: String
    val initialValue: Value
    val value: Value

    @Composable
    fun View() = DefaultFieldView()

    @Composable
    fun Content()
}

abstract class ImmutablePreviewLabField<Value> private constructor(
    override val label: String,
    override val initialValue: Value,
    state: State<Value> = mutableStateOf(initialValue),
) : PreviewLabField<Value>,
    State<Value> by state {
    constructor(
        label: String,
        initialValue: Value,
    ) : this(
        label = label,
        initialValue = initialValue,
        state = mutableStateOf(initialValue),
    )
}

abstract class MutablePreviewLabField<Value> private constructor(
    override val label: String,
    override val initialValue: Value,
    state: MutableState<Value>,
) : PreviewLabField<Value>,
    MutableState<Value> by state {
    constructor(
        label: String,
        initialValue: Value,
    ) : this(
        label = label,
        initialValue = initialValue,
        state = mutableStateOf(initialValue),
    )
}
