package me.tbsten.compose.preview.lab.mcp

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.ReadResourceResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.TextResourceContents
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import io.modelcontextprotocol.kotlin.sdk.types.toJson
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.putJsonObject
import me.tbsten.compose.preview.lab.PreviewLabPreview
import me.tbsten.compose.preview.lab.gallery.PreviewLabGalleryState
import me.tbsten.compose.preview.lab.mcp.util.json
import me.tbsten.compose.preview.lab.mcp.util.putResource
import me.tbsten.compose.preview.lab.mcp.util.serializeMap

private fun PreviewLabGalleryState.serializeState(): String = json.encodeToString(
    mapOf(
        "query" to query,
        "selectedPreviews" to selectedPreviews.map { selectedPreview ->
            mapOf(
                "groupName" to selectedPreview.groupName,
                "title" to selectedPreview.title,
            ).toJson() +
                selectedPreview.preview.serializeMap(all = false)
        },
    ).toJson(),
)

internal fun Server.updateState(state: PreviewLabGalleryState, previewList: List<PreviewLabPreview>) {
    putResource(
        uri = "$McpBaseUrl/gallery-state",
        name = "PreviewLabGallery state",
        description = """
            Retrieve the status of PreviewLabGallery. Specifically, the following information can be obtained:
            1. query ... search query.
            2. selectedPreviews ... The list of selected Previews
        """.trimIndent(),
        mimeType = "application/json",
    ) {
        ReadResourceResult(
            contents = listOf(
                TextResourceContents(
                    uri = "$McpBaseUrl/gallery-state",
                    text = state.serializeState(),
                ),
            ),
        )
    }

    addTool(
        name = "Update PreviewLagGallery.query",
        description = """
            Update the status of PreviewLabGallery. Specifically, the following information can be updated:
            1. query ... search query.
        """.trimIndent(),
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("newQuery") {
                    put("type", JsonPrimitive("string"))
                    put("description", JsonPrimitive("The new query to set."))
                }
            },
            required = listOf("newQuery"),
        ),
    ) { request ->
        val args = request.params.arguments!!
        val newQuery = args["newQuery"]!!.jsonPrimitive.content

        state.query = newQuery
        CallToolResult(
            content = listOf(
                TextContent(
                    text = "Updated. Current state: ${state.serializeState()}",
                ),
            ),
        )
    }

    addTool(
        name = "Select preview PreviewLagGallery.select",
        description = """
            Select a preview. Specifically, the following information can be updated:
            1. groupName ... The group name (featured files or "all")
            2. previewId ... The id of the preview to select. Please use the preview obtained from `Preview List (in Gallery)`.。
        """.trimIndent(),
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("groupName") {
                    put("type", JsonPrimitive("string"))
                    put("description", JsonPrimitive("""The group name (featured files or "all")"""))
                }
                putJsonObject("previewId") {
                    put("type", JsonPrimitive("string"))
                    put("description", JsonPrimitive("The id of the preview to select."))
                }
            },
            required = listOf("previewId"),
        ),
    ) { request ->
        val args = request.params.arguments ?: error("No arguments")
        val groupName = args["groupName"]?.jsonPrimitive?.content ?: "all"
        val previewId = args["previewId"]!!.jsonPrimitive.content
        val preview = previewList.find { it.id == previewId } ?: error("No preview found with id $previewId")

        state.select(groupName, preview)
        CallToolResult(
            content = listOf(
                TextContent(
                    text = "Selected. Current state: ${state.serializeState()}",
                ),
            ),
        )
    }

    addTool(
        name = "Unselect preview PreviewLabGallery.unselect",
        description = """
            Unselect all selected previews.
        """.trimIndent(),
        inputSchema = ToolSchema(
            properties = buildJsonObject { },
            required = emptyList(),
        ),
    ) {
        state.unselect()
        CallToolResult(
            content = listOf(
                TextContent(
                    text = "Unselected. Current state: ${state.serializeState()}",
                ),
            ),
        )
    }

    addTool(
        name = "Add to compare panel PreviewLabGallery.addToComparePanel",
        description = """
            Add a preview to the compare panel. Specifically, the following information is required:
            1. groupName ... The group name (featured files or "all")
            2. previewId ... The id of the preview to add. Please use the preview obtained from `Preview List (in Gallery)`.
        """.trimIndent(),
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("groupName") {
                    put("type", JsonPrimitive("string"))
                    put("description", JsonPrimitive("""The group name (featured files or "all")"""))
                }
                putJsonObject("previewId") {
                    put("type", JsonPrimitive("string"))
                    put("description", JsonPrimitive("The id of the preview to add to the compare panel."))
                }
            },
            required = listOf("previewId"),
        ),
    ) { request ->
        val args = request.params.arguments ?: error("No arguments")
        val groupName = args["groupName"]?.jsonPrimitive?.content ?: "all"
        val previewId = args["previewId"]!!.jsonPrimitive.content
        val preview = previewList.find { it.id == previewId } ?: error("No preview found with id $previewId")

        state.addToComparePanel(groupName, preview)
        CallToolResult(
            content = listOf(
                TextContent(
                    text = "Added to compare panel. Current state: ${state.serializeState()}",
                ),
            ),
        )
    }

    addTool(
        name = "Remove from compare panel PreviewLabGallery.removeFromComparePanel",
        description = """
            Remove a preview from the compare panel by its index in selectedPreviews.
            1. indexInSelectedPreviews ... The index of the preview in the selectedPreviews list (0-based).
        """.trimIndent(),
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("indexInSelectedPreviews") {
                    put("type", JsonPrimitive("number"))
                    put("description", JsonPrimitive("The index of the preview in the selectedPreviews list (0-based)."))
                }
            },
            required = listOf("indexInSelectedPreviews"),
        ),
    ) { request ->
        val args = request.params.arguments ?: error("No arguments")
        val indexInSelectedPreviews = args["indexInSelectedPreviews"]!!.jsonPrimitive.content.toInt()

        state.removeFromComparePanel(indexInSelectedPreviews)
        CallToolResult(
            content = listOf(
                TextContent(
                    text = "Removed from compare panel. Current state: ${state.serializeState()}",
                ),
            ),
        )
    }
}
