package me.tbsten.compose.preview.lab.field.modifier

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
internal class ModifierBuilderState(initialValues: ModifierFieldValueList = ModifierFieldValueList) {
    var values by mutableStateOf(initialValues)
        private set

    val addMenu = MenuState()

    fun addNewValue(value: ModifierFieldValue) {
        values = ModifierFieldValueList(values + value)
    }

    fun onRemove(index: Int, value: ModifierFieldValue) {
        values = ModifierFieldValueList(values.filterIndexed { i, _ -> i != index })
    }
}

class MenuState {
    var isAddMenuOpen by mutableStateOf(false)
        private set

    fun toggle() {
        isAddMenuOpen = !isAddMenuOpen
    }

    fun close() {
        isAddMenuOpen = false
    }
}
