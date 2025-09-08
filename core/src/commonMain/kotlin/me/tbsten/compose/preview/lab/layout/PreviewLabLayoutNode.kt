package me.tbsten.compose.preview.lab.layout

import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize

internal typealias LayoutNodeId = Long

/**
 * Represents a layout node in the preview environment for layout inspection
 * 
 * Tracks the position and size of individual layout components within the preview.
 * Used by the layout inspector to visualize component boundaries and relationships.
 * Nodes can be in resolved or unresolved states depending on whether complete
 * layout information is available.
 * 
 * @property id Unique identifier for this layout node
 * @property label Human-readable name for the node
 * @property offsetInAppRoot Position relative to the application root (if available)
 * @property size Dimensions of the layout node (if available)
 */
internal sealed interface PreviewLabLayoutNode {
    val id: LayoutNodeId
    val label: String
    val offsetInAppRoot: DpOffset?
    val size: DpSize?

    /**
     * Layout node with incomplete position or size information
     * 
     * Represents a node that hasn't been fully measured or positioned yet.
     * May be missing offset or size data during layout phases.
     */
    data class Unresolved(
        override val id: LayoutNodeId,
        override val label: String,
        override val offsetInAppRoot: DpOffset?,
        override val size: DpSize?,
    ) : PreviewLabLayoutNode

    /**
     * Layout node with complete position and size information
     * 
     * Represents a fully measured and positioned layout node.
     * Contains definitive offset and size data for accurate visualization.
     */
    data class Resolved(
        override val id: LayoutNodeId,
        override val label: String,
        override val offsetInAppRoot: DpOffset,
        override val size: DpSize,
    ) : PreviewLabLayoutNode
}

/**
 * Creates a PreviewLabLayoutNode with automatic resolution based on available data
 * 
 * Determines whether to create a Resolved or Unresolved node based on the completeness
 * of layout information. If both offset and size are available, creates a Resolved node;
 * otherwise creates an Unresolved node.
 * 
 * @param id Unique identifier for this layout node
 * @param label Human-readable name for the node
 * @param offsetInAppRoot Position relative to application root (nullable)
 * @param size Dimensions of the layout node (nullable)
 * @return Resolved node if complete data is available, Unresolved otherwise
 */
@Suppress("ktlint:standard:function-naming")
internal fun PreviewLabLayoutNode(id: LayoutNodeId, label: String, offsetInAppRoot: DpOffset?, size: DpSize?) =
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
