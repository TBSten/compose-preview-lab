package me.tbsten.compose.preview.lab.mcp

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.ReadResourceResult
import io.modelcontextprotocol.kotlin.sdk.types.TextResourceContents
import kotlinx.coroutines.CoroutineScope
import me.tbsten.compose.preview.lab.mcp.util.json
import me.tbsten.compose.preview.lab.mcp.util.notifyResourceListChanged
import me.tbsten.compose.preview.lab.mcp.util.putResource

internal fun Server.updateFeaturedFileList(featuredFileList: Map<String, List<String>>, scope: CoroutineScope,) {
    putResource(
        uri = "$McpBaseUrl/featuredFiles",
        name = "FeaturedFile list (in Gallery)",
        description = """
            List of all featured file groups in the gallery.
            Featured files are user-defined groupings of related previews for easy access.
        """.trimIndent(),
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
        putResource(
            uri = "$McpBaseUrl/featuredFiles/$featuredFile",
            name = "FeaturedFile: $featuredFile",
            description = """
                List of preview IDs belonging to the featured file group '$featuredFile'.
                Use these IDs with preview selection or comparison tools.
            """.trimIndent(),
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

    notifyResourceListChanged(scope)
}
