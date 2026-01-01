package me.tbsten.compose.preview.lab.mcp

import androidx.compose.ui.graphics.ImageBitmap
import io.modelcontextprotocol.kotlin.sdk.server.Server
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabEvent
import me.tbsten.compose.preview.lab.PreviewLabField
import me.tbsten.compose.preview.lab.previewlab.mcp.PreviewLabMcpBridge

/**
 * Implementation of [PreviewLabMcpBridge] that connects to an MCP [Server].
 *
 * This implementation uses a [PreviewLabMcpStateManager] to manage preview states
 * and provides static MCP resources and tools that don't change when previews are added or removed.
 */
@ExperimentalComposePreviewLabApi
internal class PreviewLabMcpBridgeImpl(server: Server) : PreviewLabMcpBridge {
    private val stateManager = PreviewLabMcpStateManager(server)

    override fun updateState(
        previewId: String,
        fields: List<PreviewLabField<*>>,
        events: List<PreviewLabEvent>,
        captureScreenshot: suspend () -> ImageBitmap?,
        onUpdateField: (label: String, serializedValue: String) -> Boolean,
        onClearEvents: () -> Unit,
    ) {
        val mcpState = PreviewLabMcpState(
            previewId = previewId,
            fields = fields,
            events = events,
            captureScreenshot = captureScreenshot,
            onUpdateField = onUpdateField,
            onClearEvents = onClearEvents,
        )
        stateManager.updateState(mcpState)
    }

    override fun removeState(previewId: String) {
        stateManager.removeState(previewId)
    }
}
