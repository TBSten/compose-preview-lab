package me.tbsten.compose.preview.lab.previewlab

import androidx.compose.foundation.gestures.Draggable2DState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.MutablePreviewLabField
import me.tbsten.compose.preview.lab.PreviewLabEvent
import me.tbsten.compose.preview.lab.PreviewLabField

/**
 * Class that holds the state of [PreviewLab].
 * Mainly holds state values that have changed due to user interaction during debugging.
 */
@Stable
class PreviewLabState(
    initialContentOffset: Offset = Offset.Zero,
    initialContentScale: Float = 1f,
    initialIsInspectorPanelVisible: Boolean = true,
    initialGridSize: Dp? = 40.dp,
) {
    internal var contentRootOffsetInAppRoot by mutableStateOf<DpOffset?>(null)

    /**
     * The current pan offset of the preview content.
     * Controls the x/y translation of the preview content for panning.
     *
     * @see contentDraggableState
     */
    @ExperimentalComposePreviewLabApi
    var contentOffset by mutableStateOf(initialContentOffset)

    internal val contentDraggableState = Draggable2DState { contentOffset += it }

    /**
     * The current zoom scale of the preview content.
     * Controls the zoom level for inspecting UI details. Values greater than 1.0 zoom in,
     * values less than 1.0 zoom out.
     */
    @ExperimentalComposePreviewLabApi
    var contentScale by mutableStateOf(initialContentScale)

    @ExperimentalComposePreviewLabApi
    var gridSize by mutableStateOf<Dp?>(initialGridSize)

    /**
     * Controls whether the inspector panel is visible.
     * When true, the inspector panel (Fields, Events, and additional tabs) is shown on the right side.
     */
    @ExperimentalComposePreviewLabApi
    var isInspectorPanelVisible by mutableStateOf(initialIsInspectorPanelVisible)

    /**
     * The currently selected tab index in the inspector panel.
     * Null when no tab is selected. The index corresponds to the position in the combined list
     * of default tabs and additional tabs.
     */
    @ExperimentalComposePreviewLabApi
    var selectedTabIndex by mutableStateOf<Int?>(null)
    internal var selectedEvent by mutableStateOf<PreviewLabEvent?>(null)

    internal val scope = PreviewLabScope(this)

    @ExperimentalComposePreviewLabApi
    val fields = mutableStateListOf<PreviewLabField<*>>()

    @ExperimentalComposePreviewLabApi
    val events = mutableStateListOf<PreviewLabEvent>()

    /**
     * Deselects the currently selected tab
     *
     * If there is a selected tab in the inspector tabs, this removes the selection
     * and returns to a state where no tab is selected.
     */
    @ExperimentalComposePreviewLabApi
    fun deselectTab() {
        selectedTabIndex = null
    }

    companion object {
        val Saver = mapSaver(
            save = {
                mapOf(
                    "contentOffset.x" to it.contentOffset.x,
                    "contentOffset.y" to it.contentOffset.y,
                    "contentScale" to it.contentScale,
                    "selectedTabIndex" to it.selectedTabIndex,
                    "gridSize" to it.gridSize?.value,
                )
            },
            restore = {
                PreviewLabState().apply {
                    contentOffset = Offset(it["contentOffset.x"] as Float, it["contentOffset.y"] as Float)
                    contentScale = it["contentScale"] as Float
                    selectedTabIndex = it["selectedTabIndex"] as Int?
                    gridSize = (it["gridSize"] as Float?)?.dp
                }
            },
        )
    }
}

/**
 * Finds a mutable field by its label in the PreviewLabState.
 *
 * @param Value The type of the field's value
 * @param label The label of the field to find
 * @return The field if found and matches the type, null otherwise
 *
 * Example:
 * ```kotlin
 * val intField = state.fieldOrNull<Int>("intValue")
 * if (intField != null) {
 *     intField.value = 42
 * }
 * ```
 */
@Suppress("UNCHECKED_CAST")
@ExperimentalComposePreviewLabApi
inline fun <reified Value> PreviewLabState.fieldOrNull(label: String): Lazy<MutablePreviewLabField<Value>?> = lazy {
    fields.find { it.label == label } as? MutablePreviewLabField<Value>
}

/**
 * Finds a mutable field by its label in the PreviewLabState, throwing an error if not found.
 *
 * @param Value The type of the field's value
 * @param label The label of the field to find
 * @return The field if found and matches the type
 * @throws IllegalStateException if the field is not found
 *
 * Example:
 * ```kotlin
 * val intField by state.field<Int>("intValue")
 * intField.value = 42
 * ```
 */
@ExperimentalComposePreviewLabApi
inline fun <reified Value> PreviewLabState.field(label: String): Lazy<MutablePreviewLabField<Value>> = lazy {
    fieldOrNull<Value>(label = label).value
        ?: error("Can not find update target field: label=$label")
}
