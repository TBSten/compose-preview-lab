package me.tbsten.compose.preview.lab.mcp.util

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.ReadResourceRequest
import io.modelcontextprotocol.kotlin.sdk.types.ReadResourceResult
import io.modelcontextprotocol.kotlin.sdk.types.ResourceUpdatedNotification
import io.modelcontextprotocol.kotlin.sdk.types.ResourceUpdatedNotificationParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal fun Server.putResource(
    uri: String,
    name: String,
    description: String,
    mimeType: String = "text/html",
    readHandler: suspend (ReadResourceRequest) -> ReadResourceResult,
) {
    removeResource(uri)

    addResource(
        uri = uri,
        name = name,
        description = description,
        mimeType = mimeType,
        readHandler = readHandler,
    )
}

/**
 * Sends a resource list changed notification to all connected sessions.
 * Call this when resources are added or removed.
 */
internal fun Server.notifyResourceListChanged(scope: CoroutineScope) {
    sessions.forEach { (sessionId, _) ->
        scope.launch {
            runCatching { sendResourceListChanged(sessionId) }
        }
    }
}

/**
 * Sends a resource updated notification to all connected sessions.
 * Call this when the content of a specific resource changes.
 */
internal fun Server.notifyResourceUpdated(uri: String, scope: CoroutineScope) {
    sessions.forEach { (sessionId, _) ->
        scope.launch {
            runCatching {
                sendResourceUpdated(
                    sessionId,
                    ResourceUpdatedNotification(ResourceUpdatedNotificationParams(uri)),
                )
            }
        }
    }
}

/**
 * Sends a tool list changed notification to all connected sessions.
 * Call this when tools are added or removed.
 */
internal fun Server.notifyToolListChanged(scope: CoroutineScope) {
    sessions.forEach { (sessionId, _) ->
        scope.launch {
            runCatching { sendToolListChanged(sessionId) }
        }
    }
}
