package me.tbsten.compose.preview.lab.mcp.util

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.ReadResourceRequest
import io.modelcontextprotocol.kotlin.sdk.types.ReadResourceResult

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
