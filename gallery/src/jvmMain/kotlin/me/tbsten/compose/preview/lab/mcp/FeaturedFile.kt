package me.tbsten.compose.preview.lab.mcp

import io.modelcontextprotocol.kotlin.sdk.types.ReadResourceResult
import io.modelcontextprotocol.kotlin.sdk.types.TextResourceContents
import me.tbsten.compose.preview.lab.mcp.util.json
import me.tbsten.compose.preview.lab.mcp.util.putResource

internal fun PreviewLabMcpServer.updateFeaturedFileList(featuredFileList: Map<String, List<String>>) {
    mcpServer.putResource(
        uri = "$McpBaseUrl/featuredFiles",
        name = "FeaturedFile list (in Gallery)",
        description = "",
        mimeType = "application/json",
    ) {
        ReadResourceResult(
            contents = featuredFileList.map { (featuredFile, files) ->
                TextResourceContents(
                    uri = featuredFile,
                    text = json.encodeToString(files),
                )
            },
        )
    }

    featuredFileList.forEach { (featuredFile, files) ->
        mcpServer.putResource(
            uri = "$McpBaseUrl/featuredFiles/$featuredFile",
            name = "FeaturedFile: $featuredFile",
            description = "x",
            mimeType = "application/json",
        ) {
            ReadResourceResult(
                contents = listOf(
                    TextResourceContents(
                        uri = featuredFile,
                        text = json.encodeToString(files),
                    ),
                ),
            )
        }
    }
}
