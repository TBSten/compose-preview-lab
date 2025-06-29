package me.tbsten.compose.preview.lab

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onLayoutRectChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toOffset
import io.github.takahirom.rin.rememberRetained
import kotlin.random.Random
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.coroutineScope
import me.tbsten.compose.preview.lab.event.PreviewLabEvent
import me.tbsten.compose.preview.lab.field.MutablePreviewLabField
import me.tbsten.compose.preview.lab.field.PreviewLabField
import me.tbsten.compose.preview.lab.layout.LayoutNodeId
import me.tbsten.compose.preview.lab.layout.PreviewLabLayoutNode
import me.tbsten.compose.preview.lab.util.thenIf
import me.tbsten.compose.preview.lab.util.toDpOffset
import me.tbsten.compose.preview.lab.util.toDpSize

/**
 * The scope of the [PreviewLab], which provides methods to create fields, handle events, and manage layout nodes.
 *
 * @see PreviewLabField
 * @see PreviewLabEvent
 * @see PreviewLabLayoutNode
 */
@OptIn(ExperimentalTime::class)
class PreviewLabScope internal constructor() {
    internal val fields = mutableStateListOf<PreviewLabField<*>>()
    internal val events = mutableStateListOf<PreviewLabEvent>()
    internal val layoutNodes = mutableStateListOf<PreviewLabLayoutNode>()

    internal val selectedLayoutNodeIds = mutableStateSetOf<LayoutNodeId>()
    internal val hoveredLayoutNodeIds = mutableStateSetOf<LayoutNodeId>()
    internal var onEventHandler: (Event) -> Unit = {}

    // field methods

    /**
     * Creates a mutable field that can be used to store and observe state in the Preview Lab.
     * Use [fieldValue] if you do not need to update the status.
     * This is useful, for example, for a `TextField`, where you want to use both the state value and its updates. For example, use the following.
     *
     * ```kt
     * PreviewLab {
     *   var myText by field { StringField("myText", "initialValue") }
     *
     *   TextField(
     *     value = myText,
     *     onValueChange = { myText = it },
     *   )
     * }
     * ```
     */
    @Composable
    fun <Value> field(builder: () -> MutablePreviewLabField<Value>): MutableState<Value> {
        val field = rememberRetained { builder() }
        DisposableEffect(field) {
            fields.add(field)
            onDispose { fields.remove(field) }
        }
        return field
    }

    /**
     * Creates a field that can be used to store and observe state in the Preview Lab.
     * Use this if you do not need to update the status.
     * Use when the change process does not need to be included in the Preview, for example, in the button text below.
     *
     * ```kt
     * PreviewLab {
     *   TextField(
     *     value = fieldValue { StringField("myText", "Click Me!") },
     *     onValueChange = {},
     *   )
     * }
     * ```
     */
    @Composable
    fun <Value> fieldValue(builder: () -> PreviewLabField<Value>): Value {
        val field = remember { builder() }
        DisposableEffect(field) {
            fields.add(field)
            onDispose { fields.remove(field) }
        }
        return field.value
    }

    /**
     * Records an event in the Preview Lab.
     * When onEvent is called, Toast is displayed and the event is recorded on the Event tab in the right sidebar.
     * This is useful for manual testing of events that may occur in components.
     *
     * ```kt
     * PreviewLab {
     *   MyButton(
     *     ...,
     *     onClick = { onEvent(title = "MyButton.onClick") },
     *   )
     * }
     * ```
     *
     * @param title The title of the event. This is used for the toast display and also appears in the event list on the Events tab.
     * @param description It will not appear on the toast, but it will appear on the event tab. If you have a lot of information, use description instead of title to make the debug UI easier to read.
     */
    fun onEvent(title: String, description: String? = null) {
        val event = PreviewLabEvent(title = title, description = description)
        events.add(event)
        onEventHandler.invoke(Event.ShowEventToast(event = event))
    }

    // layoutNode methods
    internal fun addLayoutNode(node: PreviewLabLayoutNode) {
        layoutNodes.add(node)
    }

    internal fun removeLayoutNode(id: Long) {
        layoutNodes.indexOfFirst { it.id == id }
            .also { if (it != -1) layoutNodes.removeAt(it) }
    }

    internal fun putLayoutNode(id: Long, label: String, offsetInAppRoot: DpOffset?, size: DpSize?) {
        val nodeIndex = layoutNodes.indexOfFirst { it.id == id }
        if (nodeIndex == -1) {
            addLayoutNode(
                PreviewLabLayoutNode(
                    id = id,
                    label = label,
                    offsetInAppRoot = offsetInAppRoot,
                    size = size,
                ),
            )
        } else {
            val new = PreviewLabLayoutNode(
                id = id,
                label = label,
                offsetInAppRoot = offsetInAppRoot ?: layoutNodes[nodeIndex].offsetInAppRoot,
                size = size ?: layoutNodes[nodeIndex].size,
            )
            layoutNodes[nodeIndex] = new
        }
    }

    internal fun toggleLayoutNodeSelect(id: LayoutNodeId) {
        if (selectedLayoutNodeIds.contains(id)) {
            selectedLayoutNodeIds.remove(id)
        } else {
            selectedLayoutNodeIds.add(id)
        }
    }

    internal fun onHoverMouseLayoutNode(id: LayoutNodeId) {
        hoveredLayoutNodeIds.add(id)
    }

    internal fun onLeaveMouseLayoutNode(id: LayoutNodeId) {
        hoveredLayoutNodeIds.remove(id)
    }

    internal fun onShowPaddingViewer(hoveredNodeId: LayoutNodeId) {
        println("onShowPaddingViewer: $hoveredNodeId")
    }

    internal fun onHidePaddingViewer(hoveredNodeId: LayoutNodeId) {
        println("onHidePaddingViewer: $hoveredNodeId")
    }

    @Composable
    internal fun HandleEvents(onEvent: (Event) -> Unit) {
        onEventHandler = rememberUpdatedState(onEvent).value
    }

    internal sealed interface Event {
        data class ShowEventToast(val event: PreviewLabEvent) : Event
    }
}

/**
 * Composable with this modifier can display various information in the sidebar when a layout tab is selected.
 *
 * ```kt
 * @Composable
 * fun MyButton(text: String) {
 *   Surface(
 *     modifier = Modifier.layoutLab("MyButton.Root").padding(16.dp),
 *   ) {
 *     Text(text, modifier = Modifier.layoutLab("MyButton.Text"))
 *   }
 * }
 * ```
 */
@ExperimentalComposePreviewLabApi
@Composable
internal fun Modifier.layoutLab(label: String): Modifier = composed(
    inspectorInfo = {
        name = "layoutLab"
        properties["label"] = label
    },
) {
    val state = LocalPreviewLabState.current ?: return@composed this
    val scope = state.scope

    val density = LocalDensity.current
    val id = remember(label) {
        Random.nextLong()
    }
    DisposableEffect(scope, id) {
        onDispose {
            scope.removeLayoutNode(id)
        }
    }

    val isSelected = scope.selectedLayoutNodeIds.any { it == id }
    val isHovered = scope.hoveredLayoutNodeIds.any { it == id }

    onLayoutRectChanged {
        it.boundsInRoot.also {
            scope.putLayoutNode(
                id = id,
                label = label,
                offsetInAppRoot = it.topLeft.toOffset().toDpOffset(density),
                size = it.size.toDpSize(density),
            )
        }
    }.thenIf(
        state.selectedTabIndex == 2,
    ) {
        pointerInput(Unit) {
            coroutineScope {
                detectTapGestures(
                    onTap = {
                        scope.toggleLayoutNodeSelect(id = id)
                    },
                )
            }
        }.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    if (event.type == PointerEventType.Enter) {
                        scope.onHoverMouseLayoutNode(id = id)
                    } else if (event.type == PointerEventType.Exit) {
                        scope.onLeaveMouseLayoutNode(id = id)
                    }
                }
            }
        }.thenIf(isSelected || isHovered) {
            border(
                2.dp,
                Color.Red.copy(alpha = if (isSelected) 1.0f else 0.5f),
            )
        }
    }
}
