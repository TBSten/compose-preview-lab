package me.tbsten.compose.preview.lab.mcp.util

import io.modelcontextprotocol.kotlin.sdk.types.toJson
import me.tbsten.compose.preview.lab.PreviewLabPreview

internal fun PreviewLabPreview.serializeMap(all: Boolean) = buildMap {
    val defaults = mapOf(
        "id" to id,
        "displayName" to displayName,
        "filePath" to filePath,
        "startLineNumber" to startLineNumber,
    )
    putAll(defaults)

    if (all) put("code", code)
}.toJson()
