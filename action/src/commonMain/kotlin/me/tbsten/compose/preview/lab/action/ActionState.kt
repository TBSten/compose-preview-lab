package me.tbsten.compose.preview.lab.action

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabAction

/**
 * State holder for managing [PreviewLabAction]s in the Preview Lab.
 *
 * This class manages a collection of actions that can be executed from the Preview Lab UI.
 * Actions provide a way to trigger custom behavior with configurable parameters during preview.
 *
 * @see PreviewLabAction
 */
@Stable
@ExperimentalComposePreviewLabApi
class ActionState {
    private val _actions = mutableStateListOf<PreviewLabAction<*>>()

    /**
     * The list of registered actions.
     */
    val actions: List<PreviewLabAction<*>> get() = _actions

    /**
     * Adds an action to the state.
     *
     * @param action The action to add
     */
    fun add(action: PreviewLabAction<*>) {
        _actions.add(action)
    }

    /**
     * Removes an action from the state.
     *
     * @param action The action to remove
     */
    fun remove(action: PreviewLabAction<*>) {
        _actions.remove(action)
    }

    /**
     * Clears all registered actions.
     */
    fun clear() {
        _actions.clear()
    }
}
