package me.tbsten.compose.preview.lab.previewlab.mcp

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.ImageBitmap
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabEvent
import me.tbsten.compose.preview.lab.PreviewLabField

/**
 * Bridge interface for communicating PreviewLab state to MCP server.
 *
 * This interface allows PreviewLab to expose its fields, events, and screenshot functionality
 * to external MCP (Model Context Protocol) servers without direct dependency on MCP implementation.
 */
@ExperimentalComposePreviewLabApi
interface PreviewLabMcpBridge {
    /**
     * Called when the PreviewLab state changes.
     * Implementations should update MCP resources accordingly.
     *
     * @param previewId Unique identifier for this PreviewLab instance
     * @param fields Current list of fields in PreviewLab
     * @param events Current list of events in PreviewLab
     * @param captureScreenshot Function to capture a screenshot of the preview content
     * @param onUpdateField Callback to update a field's value by label
     * @param onClearEvents Callback to clear all events
     */
    fun updateState(
        previewId: String,
        fields: List<PreviewLabField<*>>,
        events: List<PreviewLabEvent>,
        captureScreenshot: suspend () -> ImageBitmap?,
        onUpdateField: (label: String, serializedValue: String) -> Boolean,
        onClearEvents: () -> Unit,
    )

    /**
     * Called when a PreviewLab instance is disposed.
     * Implementations should remove the corresponding MCP resources.
     *
     * @param previewId Unique identifier for the disposed PreviewLab instance
     */
    fun removeState(previewId: String)

    companion object {
        /**
         * No-op implementation for platforms that don't support MCP.
         */
        val NoOp: PreviewLabMcpBridge = object : PreviewLabMcpBridge {
            override fun updateState(
                previewId: String,
                fields: List<PreviewLabField<*>>,
                events: List<PreviewLabEvent>,
                captureScreenshot: suspend () -> ImageBitmap?,
                onUpdateField: (label: String, serializedValue: String) -> Boolean,
                onClearEvents: () -> Unit,
            ) = Unit

            override fun removeState(previewId: String) = Unit
        }
    }
}

/**
 * CompositionLocal for providing PreviewLabMcpBridge.
 *
 * By default, this provides [PreviewLabMcpBridge.NoOp] which does nothing.
 * On JVM platforms with MCP support, this should be overridden with an actual implementation.
 */
@ExperimentalComposePreviewLabApi
val LocalPreviewLabMcpBridge = compositionLocalOf<PreviewLabMcpBridge> { PreviewLabMcpBridge.NoOp }
