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
 * This implementation registers MCP resources and tools for each PreviewLab instance,
 * allowing external tools to query and manipulate PreviewLab state.
 */
@ExperimentalComposePreviewLabApi
internal class PreviewLabMcpBridgeImpl(private val server: Server,) : PreviewLabMcpBridge {

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
        server.updatePreviewLabState(mcpState)
    }

    override fun removeState(previewId: String) {
        server.removePreviewLabState(previewId)
    }
}
