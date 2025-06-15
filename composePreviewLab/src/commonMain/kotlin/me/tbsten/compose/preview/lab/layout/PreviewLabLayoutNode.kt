package me.tbsten.compose.preview.lab.layout

import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize

internal typealias LayoutNodeId = Long

internal sealed interface PreviewLabLayoutNode {
    val id: LayoutNodeId
    val label: String
    val offsetInAppRoot: DpOffset?
    val size: DpSize?

    fun offsetInContentRoot(contentRootOffset: DpOffset): DpOffset? = TODO("offsetInContentRoot")

    data class Unresolved(
        override val id: LayoutNodeId,
        override val label: String,
        override val offsetInAppRoot: DpOffset?,
        override val size: DpSize?,
    ) : PreviewLabLayoutNode

    data class Resolved(
        override val id: LayoutNodeId,
        override val label: String,
        override val offsetInAppRoot: DpOffset,
        override val size: DpSize,
    ) : PreviewLabLayoutNode
}

internal fun PreviewLabLayoutNode(id: LayoutNodeId, label: String, offsetInAppRoot: DpOffset?, size: DpSize?,) =
    if (offsetInAppRoot != null && size != null) {
        PreviewLabLayoutNode.Resolved(
            id = id,
            label = label,
            offsetInAppRoot = offsetInAppRoot,
            size = size,
        )
    } else {
        PreviewLabLayoutNode.Unresolved(
            id = id,
            label = label,
            offsetInAppRoot = offsetInAppRoot,
            size = size,
        )
    }
