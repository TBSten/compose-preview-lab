package me.tbsten.compose.preview.lab.me

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toIntSize
import me.tbsten.compose.preview.lab.me.event.PreviewLabEvent
import me.tbsten.compose.preview.lab.me.field.PreviewLabField
import me.tbsten.compose.preview.lab.me.layout.PreviewLabLayoutNode
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class PreviewLabScope internal constructor() {
    internal val fields = mutableStateListOf<PreviewLabField<*>>()
    internal val events = mutableStateListOf<PreviewLabEvent>()
    internal val layoutNodes = mutableStateListOf<PreviewLabLayoutNode>()

    internal val selectedLayoutNode = mutableStateListOf<PreviewLabLayoutNode>()

    @Composable
    fun <Value> field(builder: () -> PreviewLabField<Value>): MutableState<Value> {
        val field = remember { builder() }
        DisposableEffect(field) {
            fields.add(field)
            onDispose { fields.remove(field) }
        }
        return field
    }

    // field methods
    @Composable
    fun <Value> fieldValue(builder: () -> PreviewLabField<Value>) =
        field(builder).value

    fun onEvent(title: String, description: String? = null) {
        events.add(PreviewLabEvent(title = title, description = description))
    }

    // layoutNode methods
    internal fun addLayoutNode(node: PreviewLabLayoutNode) {
        layoutNodes.add(node)
    }

    internal fun removeLayoutNode(node: PreviewLabLayoutNode) {
        layoutNodes.remove(node)
    }

    internal fun updateLayoutNode(
        node: PreviewLabLayoutNode,
        offsetInAppRoot: DpOffset? = null,
        size: DpSize? = null,
    ) {
        val nodeIndex = layoutNodes.indexOf(node)
        if (nodeIndex != -1) {
            layoutNodes[nodeIndex] = PreviewLabLayoutNode(
                label = node.label,
                offsetInAppRoot = offsetInAppRoot ?: node.offsetInAppRoot,
                size = size ?: node.size,
                resizable = node.resizable,
            )
        }
    }
}

@Composable
fun Modifier.testLayout(
    label: String,
    resizable: Boolean = false,
): Modifier {
    val scope = LocalPreviewLabScope.current ?: return this

    val density = LocalDensity.current
    val layoutNode = remember(label) {
        PreviewLabLayoutNode(
            label = label,
            offsetInAppRoot = null,
            size = null,
            resizable = resizable,
        )
    }
    DisposableEffect(scope, layoutNode) {
        scope.addLayoutNode(layoutNode)
        onDispose {
            scope.removeLayoutNode(layoutNode)
        }
    }
    return then(
        Modifier.onPlaced {
            it.boundsInRoot().also {
                scope.updateLayoutNode(
                    node = layoutNode,
                    offsetInAppRoot = it.topLeft.toDpOffset(density),
                    size = it.size.toIntSize().toDpSize(density),
                )
            }
        }.onSizeChanged {
            scope.updateLayoutNode(
                node = layoutNode,
                size = it.toDpSize(density),
            )
        }
    )
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
