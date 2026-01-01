package me.tbsten.compose.preview.lab.mcp

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.ReadResourceResult
import io.modelcontextprotocol.kotlin.sdk.types.TextResourceContents
import me.tbsten.compose.preview.lab.PreviewLabPreview
import me.tbsten.compose.preview.lab.mcp.util.json
import me.tbsten.compose.preview.lab.mcp.util.putResource
import me.tbsten.compose.preview.lab.mcp.util.serializeMap

internal fun Server.updatePreviewList(previewList: List<PreviewLabPreview>) {
    putResource(
        uri = "$McpBaseUrl/previews",
        name = "Preview List (in Gallery)",
        description = """
            List of all available previews in the gallery.
            Each preview includes: id, displayName, filePath, and startLineNumber.
            Use preview IDs with other tools to select, compare, or inspect previews.
        """.trimIndent(),
        mimeType = "application/json",
    ) {
        ReadResourceResult(
            contents = previewList.map { preview ->
                TextResourceContents(
                    uri = preview.uri(),
                    text = json.encodeToString(
                        previewList.map { it.serializeMap(all = false) },
                    ),
                )
            },
        )
    }

    previewList.forEach { preview ->
        putResource(
            uri = preview.uri(),
            name = "PreviewLab Preview: " + preview.displayName,
            description = """
                Detailed information for preview '${preview.displayName}'.
                Includes: id, displayName, filePath, startLineNumber, and source code.
            """.trimIndent(),
            mimeType = "application/json",
        ) {
            ReadResourceResult(
                contents = listOf(
                    TextResourceContents(
                        uri = it.uri,
                        text = json.encodeToString(preview.serializeMap(all = true)),
                    ),
                ),
            )
        }
    }
}

private fun PreviewLabPreview.uri() = "$McpBaseUrl/previews/$id"
