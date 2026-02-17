package me.tbsten.compose.preview.lab.previewlab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.serialization.json.Json
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.MutablePreviewLabField

private val mcpBridgeJson = Json {
    ignoreUnknownKeys = true
    isLenient = false
}

/**
 * Effect that notifies the MCP bridge of PreviewLab state changes.
 */
@OptIn(ExperimentalComposePreviewLabApi::class)
@Composable
internal fun McpBridgeEffect(
    mcpBridge: me.tbsten.compose.preview.lab.previewlab.mcp.PreviewLabMcpBridge,
    previewId: String,
    state: PreviewLabState,
    captureScreenshot: suspend () -> androidx.compose.ui.graphics.ImageBitmap?,
) {
    // Notify MCP bridge when fields or events change
    LaunchedEffect(mcpBridge, previewId, state) {
        snapshotFlow { state.fields.toList() to state.events.toList() }
            .collect { (fields, events) ->
                mcpBridge.updateState(
                    previewId = previewId,
                    fields = fields,
                    events = events,
                    captureScreenshot = captureScreenshot,
                    onUpdateField = { label, serializedValue ->
                        updateFieldValue(state, label, serializedValue)
                    },
                    onClearEvents = {
                        state.events.clear()
                    },
                )
            }
    }

    // Remove state when disposed
    DisposableEffect(mcpBridge, previewId) {
        onDispose {
            mcpBridge.removeState(previewId)
        }
    }
}

/**
 * Updates a field value from a serialized JSON string.
 */
@OptIn(ExperimentalComposePreviewLabApi::class)
@Suppress("UNCHECKED_CAST")
private fun updateFieldValue(state: PreviewLabState, label: String, serializedValue: String): Boolean {
    val field = state.fields.find { it.label == label } ?: return false
    if (field !is MutablePreviewLabField<*>) return false

    val serializer = field.serializer() ?: return false

    return try {
        val newValue = mcpBridgeJson.decodeFromString(serializer, serializedValue)
        (field as MutablePreviewLabField<Any?>).value = newValue
        true
    } catch (e: Exception) {
        println("[PreviewLab] Failed to update field '$label': ${e.message}")
        false
    }
}
