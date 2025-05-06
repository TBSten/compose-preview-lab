package me.tbsten.compose.preview.lab.me

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onLayoutRectChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toOffset
import kotlinx.coroutines.coroutineScope
import me.tbsten.compose.preview.lab.me.event.PreviewLabEvent
import me.tbsten.compose.preview.lab.me.field.MutablePreviewLabField
import me.tbsten.compose.preview.lab.me.field.PreviewLabField
import me.tbsten.compose.preview.lab.me.layout.LayoutNodeId
import me.tbsten.compose.preview.lab.me.layout.PreviewLabLayoutNode
import me.tbsten.compose.preview.lab.me.util.thenIf
import kotlin.random.Random
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class PreviewLabScope internal constructor() {
    internal val fields = mutableStateListOf<PreviewLabField<*>>()
    internal val events = mutableStateListOf<PreviewLabEvent>()
    internal val layoutNodes = mutableStateListOf<PreviewLabLayoutNode>()

    internal val selectedLayoutNodeIds = mutableStateSetOf<LayoutNodeId>()
    internal val hoveredLayoutNodeIds = mutableStateSetOf<LayoutNodeId>()

    // field methods
    @Composable
    fun <Value> field(builder: () -> MutablePreviewLabField<Value>): MutableState<Value> {
        val field = remember { builder() }
        DisposableEffect(field) {
            fields.add(field)
            onDispose { fields.remove(field) }
        }
        return field
    }

    @Composable
    fun <Value> fieldValue(builder: () -> PreviewLabField<Value>): Value {
        val field = remember { builder() }
        DisposableEffect(field) {
            fields.add(field)
            onDispose { fields.remove(field) }
        }
        return field.value
    }

    fun onEvent(title: String, description: String? = null) {
        events.add(PreviewLabEvent(title = title, description = description))
    }

    // layoutNode methods
    internal fun addLayoutNode(node: PreviewLabLayoutNode) {
        layoutNodes.add(node)
    }

    internal fun removeLayoutNode(id: Long) {
        layoutNodes.indexOfFirst { it.id == id }
            .also { if (it != -1) layoutNodes.removeAt(it) }
    }

    internal fun putLayoutNode(
        id: Long,
        label: String,
        offsetInAppRoot: DpOffset?,
        size: DpSize?,
    ) {
        val nodeIndex = layoutNodes.indexOfFirst { it.id == id }
        if (nodeIndex == -1) {
            addLayoutNode(
                PreviewLabLayoutNode(
                    id = id,
                    label = label,
                    offsetInAppRoot = offsetInAppRoot,
                    size = size,
                )
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

    internal fun toggleLayoutNodeSelect(
        id: LayoutNodeId,
    ) {
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
}

@Composable
fun Modifier.layoutLab(
    label: String,
): Modifier = composed(
    inspectorInfo = {
        name = "layoutLab"
        properties["label"] = label
    }
) {
    val scope = LocalPreviewLabScope.current ?: return@composed this

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
    }.onLayoutRectChanged {
        it.boundsInRoot.also {
            scope.putLayoutNode(
                id = id,
                label = label,
                offsetInAppRoot = it.topLeft.toOffset().toDpOffset(density),
                size = it.size.toDpSize(density),
            )
        }
    }.thenIf(isSelected || isHovered) {
        border(
            2.dp,
            Color.Red.copy(alpha = if (isSelected) 1.0f else 0.5f),
        )
    }
}

private fun Offset.toDpOffset(density: Density): DpOffset = with(density) {
    DpOffset(
        x = x.toDp(),
        y = y.toDp(),
    )
}

private fun IntSize.toDpSize(density: Density): DpSize = with(density) {
    DpSize(
        width = width.toDp(),
        height = height.toDp(),
    )
}
