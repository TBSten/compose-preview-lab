package me.tbsten.compose.preview.lab

import androidx.compose.foundation.gestures.Draggable2DState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.DpOffset
import me.tbsten.compose.preview.lab.event.PreviewLabEvent
import me.tbsten.compose.preview.lab.field.PreviewLabField

/**
 * Class that holds the state of [PreviewLab].
 * Mainly holds state values that have changed due to user interaction during debugging.
 */
@Stable
class PreviewLabState {
    internal var contentRootOffsetInAppRoot by mutableStateOf<DpOffset?>(null)

    /**
     * The current pan offset of the preview content.
     * Controls the x/y translation of the preview content for panning.
     *
     * @see contentDraggableState
     */
    @ExperimentalComposePreviewLabApi
    var contentOffset by mutableStateOf(Offset.Zero)

    internal val contentDraggableState = Draggable2DState { contentOffset += it }

    /**
     * The current zoom scale of the preview content.
     * Controls the zoom level for inspecting UI details. Values greater than 1.0 zoom in,
     * values less than 1.0 zoom out.
     */
    @ExperimentalComposePreviewLabApi
    var contentScale by mutableStateOf(1f)

    /**
     * Controls whether the inspector panel is visible.
     * When true, the inspector panel (Fields, Events, and additional tabs) is shown on the right side.
     */
    @ExperimentalComposePreviewLabApi
    var isInspectorPanelVisible by mutableStateOf(true)

    /**
     * The currently selected tab index in the inspector panel.
     * Null when no tab is selected. The index corresponds to the position in the combined list
     * of default tabs and additional tabs.
     */
    @ExperimentalComposePreviewLabApi
    var selectedTabIndex by mutableStateOf<Int?>(null)
    internal var selectedEvent by mutableStateOf<PreviewLabEvent?>(null)

    @InternalComposePreviewLabApi
    val scope: PreviewLabScope = PreviewLabScope(this)

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
    internal fun deselectTab() {
        selectedTabIndex = null
    }

    companion object {
        val Saver = mapSaver(
            save = {
                mapOf(
                    "contentRootOffsetInAppRoot?.x" to it.contentRootOffsetInAppRoot?.x,
                    "contentRootOffsetInAppRoot?.y" to it.contentRootOffsetInAppRoot?.y,
                    "contentOffset.x" to it.contentOffset.x,
                    "contentOffset.y" to it.contentOffset.y,
                    "contentScale" to it.contentScale,
                    "selectedTabIndex" to it.selectedTabIndex,
                )
            },
            restore = {
                PreviewLabState().apply {
                    contentOffset = it["contentOffset"] as Offset
                    contentScale = it["contentScale"] as Float
                    selectedTabIndex = it["selectedTabIndex"] as Int
                }
            },
        )
    }
}
