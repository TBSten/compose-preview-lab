package me.tbsten.compose.preview.lab.field.modifier

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
internal class ModifierBuilderState(initialValues: ModifierFieldValueList = ModifierFieldValueList) {
    public var values: ModifierFieldValueList by mutableStateOf(initialValues)
        private set

    val addMenu = MenuState()

    public fun addNewValue(value: ModifierFieldValue) {
        values = ModifierFieldValueList(values + value)
    }

    public fun onRemove(index: Int, value: ModifierFieldValue) {
        values = ModifierFieldValueList(values.filterIndexed { i, _ -> i != index })
    }
}

public class MenuState {
    public var isAddMenuOpen: Boolean by mutableStateOf(false)
        private set

    public fun toggle() {
        isAddMenuOpen = !isAddMenuOpen
    }

    public fun close() {
        isAddMenuOpen = false
    }
}
