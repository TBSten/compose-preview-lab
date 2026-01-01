package me.tbsten.compose.preview.lab.mcp

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.ImageContent
import io.modelcontextprotocol.kotlin.sdk.types.ReadResourceResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.TextResourceContents
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.coroutines.runBlocking
import kotlin.time.ExperimentalTime
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.putJsonObject
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.MutablePreviewLabField
import me.tbsten.compose.preview.lab.PreviewLabEvent
import me.tbsten.compose.preview.lab.PreviewLabField
import me.tbsten.compose.preview.lab.mcp.util.json
import me.tbsten.compose.preview.lab.mcp.util.putResource
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

/**
 * Holds the state information for a single PreviewLab instance that can be accessed via MCP.
 */
internal class PreviewLabMcpState(
    val previewId: String,
    val fields: List<PreviewLabField<*>>,
    val events: List<PreviewLabEvent>,
    val captureScreenshot: suspend () -> ImageBitmap?,
    val onUpdateField: (label: String, serializedValue: String) -> Boolean,
    val onClearEvents: () -> Unit,
)

/**
 * Manager for PreviewLab MCP state.
 * Stores all preview states and provides static resources/tools to access them.
 */
internal class PreviewLabMcpStateManager(private val server: Server) {
    private val states = ConcurrentHashMap<String, PreviewLabMcpState>()
    private var resourcesAndToolsRegistered = false

    fun updateState(state: PreviewLabMcpState) {
        states[state.previewId] = state
        ensureResourcesAndToolsRegistered()
    }

    fun removeState(previewId: String) {
        states.remove(previewId)
    }

    fun getState(previewId: String): PreviewLabMcpState? = states[previewId]

    fun getAllStates(): Map<String, PreviewLabMcpState> = states.toMap()

    @OptIn(ExperimentalComposePreviewLabApi::class)
    private fun ensureResourcesAndToolsRegistered() {
        if (resourcesAndToolsRegistered) return
        resourcesAndToolsRegistered = true

        registerResources()
        registerTools()
    }

    @OptIn(ExperimentalTime::class)
    private fun registerResources() {
        val baseUrl = "$McpBaseUrl/preview-lab"

        // Resource: List all available previews
        server.putResource(
            uri = "$baseUrl/list",
            name = "PreviewLab list",
            description = """
                List of all available PreviewLab instances.
                Returns preview IDs that can be used with other resources and tools.
            """.trimIndent(),
            mimeType = "application/json",
        ) {
            ReadResourceResult(
                contents = listOf(
                    TextResourceContents(
                        uri = "$baseUrl/list",
                        text = json.encodeToString(
                            JsonArray(
                                states.keys.map { JsonPrimitive(it) },
                            ),
                        ),
                    ),
                ),
            )
        }

        // Resource: Get fields for a specific preview
        server.putResource(
            uri = "$baseUrl/fields",
            name = "PreviewLab fields",
            description = """
                Get the list of fields for a specific PreviewLab instance.
                Use with previewId query parameter (e.g., preview-lab/fields?previewId=xxx).
                Each field includes: label, value (toString), and whether it is mutable.
            """.trimIndent(),
            mimeType = "application/json",
        ) { request ->
            val previewId = extractPreviewId(request.params.uri)
            val state = previewId?.let { states[it] }

            if (state == null) {
                ReadResourceResult(
                    contents = listOf(
                        TextResourceContents(
                            uri = "$baseUrl/fields",
                            text = json.encodeToString(
                                buildJsonObject {
                                    put("error", JsonPrimitive("Preview not found"))
                                    put("availablePreviews", JsonArray(states.keys.map { JsonPrimitive(it) }))
                                },
                            ),
                        ),
                    ),
                )
            } else {
                ReadResourceResult(
                    contents = listOf(
                        TextResourceContents(
                            uri = "$baseUrl/fields",
                            text = json.encodeToString(
                                JsonArray(
                                    state.fields.map { field ->
                                        mapOf(
                                            "label" to field.label,
                                            "value" to field.value.toString(),
                                            "isMutable" to (field is MutablePreviewLabField<*>),
                                        ).toJsonElement()
                                    },
                                ),
                            ),
                        ),
                    ),
                )
            }
        }

        // Resource: Get events for a specific preview
        server.putResource(
            uri = "$baseUrl/events",
            name = "PreviewLab events",
            description = """
                Get the list of events for a specific PreviewLab instance.
                Use with previewId query parameter (e.g., preview-lab/events?previewId=xxx).
                Each event includes: title, description, and timestamp.
            """.trimIndent(),
            mimeType = "application/json",
        ) { request ->
            val previewId = extractPreviewId(request.params.uri)
            val state = previewId?.let { states[it] }

            if (state == null) {
                ReadResourceResult(
                    contents = listOf(
                        TextResourceContents(
                            uri = "$baseUrl/events",
                            text = json.encodeToString(
                                buildJsonObject {
                                    put("error", JsonPrimitive("Preview not found"))
                                    put("availablePreviews", JsonArray(states.keys.map { JsonPrimitive(it) }))
                                },
                            ),
                        ),
                    ),
                )
            } else {
                ReadResourceResult(
                    contents = listOf(
                        TextResourceContents(
                            uri = "$baseUrl/events",
                            text = json.encodeToString(
                                JsonArray(
                                    state.events.map { event ->
                                        mapOf(
                                            "title" to event.title,
                                            "description" to (event.description ?: ""),
                                            "createdAt" to event.createAt.toString(),
                                        ).toJsonElement()
                                    },
                                ),
                            ),
                        ),
                    ),
                )
            }
        }
    }

    @OptIn(ExperimentalComposePreviewLabApi::class)
    private fun registerTools() {
        // Tool: Update field value
        server.addTool(
            name = "Update PreviewLab field",
            description = """
                Update a field value in a PreviewLab instance.
                The value should be provided as a JSON-serialized string that matches the field's serializer.
            """.trimIndent(),
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    putJsonObject("previewId") {
                        put("type", JsonPrimitive("string"))
                        put("description", JsonPrimitive("The ID of the preview to update."))
                    }
                    putJsonObject("label") {
                        put("type", JsonPrimitive("string"))
                        put("description", JsonPrimitive("The label of the field to update."))
                    }
                    putJsonObject("serializedValue") {
                        put("type", JsonPrimitive("string"))
                        put("description", JsonPrimitive("The new value as a JSON-serialized string."))
                    }
                },
                required = listOf("previewId", "label", "serializedValue"),
            ),
        ) { request ->
            val args = request.params.arguments ?: error("No arguments")
            val previewId = args["previewId"]?.jsonPrimitive?.content ?: error("Missing previewId")
            val label = args["label"]?.jsonPrimitive?.content ?: error("Missing label")
            val serializedValue = args["serializedValue"]?.jsonPrimitive?.content
                ?: error("Missing serializedValue")

            val state = states[previewId]
            if (state == null) {
                CallToolResult(
                    content = listOf(
                        TextContent(
                            text = "Preview '$previewId' not found. Available previews: ${states.keys}",
                        ),
                    ),
                )
            } else {
                val success = state.onUpdateField(label, serializedValue)
                CallToolResult(
                    content = listOf(
                        TextContent(
                            text = if (success) {
                                "Field '$label' updated successfully."
                            } else {
                                "Failed to update field '$label'. The field may not exist, " +
                                    "may not be mutable, or the value format may be invalid."
                            },
                        ),
                    ),
                )
            }
        }

        // Tool: Update field value with test value
        server.addTool(
            name = "Update PreviewLab field with test value",
            description = """
                Update a field value in a PreviewLab instance using one of its testValues().
                Specify the index (0-based) of the test value to use.
            """.trimIndent(),
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    putJsonObject("previewId") {
                        put("type", JsonPrimitive("string"))
                        put("description", JsonPrimitive("The ID of the preview to update."))
                    }
                    putJsonObject("label") {
                        put("type", JsonPrimitive("string"))
                        put("description", JsonPrimitive("The label of the field to update."))
                    }
                    putJsonObject("testValueIndex") {
                        put("type", JsonPrimitive("integer"))
                        put("description", JsonPrimitive("The 0-based index of the test value to use."))
                    }
                },
                required = listOf("previewId", "label", "testValueIndex"),
            ),
        ) { request ->
            val args = request.params.arguments ?: error("No arguments")
            val previewId = args["previewId"]?.jsonPrimitive?.content ?: error("Missing previewId")
            val label = args["label"]?.jsonPrimitive?.content ?: error("Missing label")
            val testValueIndex = args["testValueIndex"]?.jsonPrimitive?.content?.toIntOrNull()
                ?: error("Missing or invalid testValueIndex")

            val state = states[previewId]
            if (state == null) {
                CallToolResult(
                    content = listOf(
                        TextContent(
                            text = "Preview '$previewId' not found. Available previews: ${states.keys}",
                        ),
                    ),
                )
            } else {
                val field = state.fields.find { it.label == label }
                if (field == null) {
                    CallToolResult(
                        content = listOf(
                            TextContent(
                                text = "Field '$label' not found. Available fields: ${
                                    state.fields.map { it.label }
                                }",
                            ),
                        ),
                    )
                } else {
                    val testValues = field.testValues()
                    if (testValueIndex < 0 || testValueIndex >= testValues.size) {
                        CallToolResult(
                            content = listOf(
                                TextContent(
                                    text = "Test value index $testValueIndex is out of range. " +
                                        "Available indices: 0..${testValues.size - 1} " +
                                        "(testValues: ${testValues.map { it.toString() }})",
                                ),
                            ),
                        )
                    } else {
                        val testValue = testValues[testValueIndex]
                        val serializer = field.serializer()
                        if (serializer == null) {
                            CallToolResult(
                                content = listOf(
                                    TextContent(
                                        text = "Field '$label' does not have a serializer. " +
                                            "Cannot update with test value.",
                                    ),
                                ),
                            )
                        } else {
                            @Suppress("UNCHECKED_CAST")
                            val serializedValue = try {
                                json.encodeToString(
                                    serializer as kotlinx.serialization.KSerializer<Any?>,
                                    testValue,
                                )
                            } catch (e: Exception) {
                                null
                            }

                            if (serializedValue == null) {
                                CallToolResult(
                                    content = listOf(
                                        TextContent(
                                            text = "Failed to serialize test value '$testValue'.",
                                        ),
                                    ),
                                )
                            } else {
                                val success = state.onUpdateField(label, serializedValue)
                                CallToolResult(
                                    content = listOf(
                                        TextContent(
                                            text = if (success) {
                                                "Field '$label' updated to test value [$testValueIndex]: $testValue"
                                            } else {
                                                "Failed to update field '$label' with test value."
                                            },
                                        ),
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tool: Take screenshot
        server.addTool(
            name = "Take PreviewLab screenshot",
            description = """
                Capture a screenshot of the preview content in a PreviewLab instance.
                Returns the screenshot as a PNG image.
            """.trimIndent(),
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    putJsonObject("previewId") {
                        put("type", JsonPrimitive("string"))
                        put("description", JsonPrimitive("The ID of the preview to capture."))
                    }
                },
                required = listOf("previewId"),
            ),
        ) { request ->
            val args = request.params.arguments ?: error("No arguments")
            val previewId = args["previewId"]?.jsonPrimitive?.content ?: error("Missing previewId")

            val state = states[previewId]
            if (state == null) {
                CallToolResult(
                    content = listOf(
                        TextContent(
                            text = "Preview '$previewId' not found. Available previews: ${states.keys}",
                        ),
                    ),
                )
            } else {
                val imageBitmap = runBlocking { state.captureScreenshot() }
                if (imageBitmap != null) {
                    val base64Image = imageBitmap.encodeToBase64Png()
                    CallToolResult(
                        content = listOf(
                            ImageContent(
                                data = base64Image,
                                mimeType = "image/png",
                            ),
                        ),
                    )
                } else {
                    CallToolResult(
                        content = listOf(
                            TextContent(
                                text = "Failed to capture screenshot.",
                            ),
                        ),
                    )
                }
            }
        }

        // Tool: Clear all events
        server.addTool(
            name = "Clear PreviewLab events",
            description = """
                Clear all recorded events in a PreviewLab instance.
            """.trimIndent(),
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    putJsonObject("previewId") {
                        put("type", JsonPrimitive("string"))
                        put("description", JsonPrimitive("The ID of the preview to clear events for."))
                    }
                },
                required = listOf("previewId"),
            ),
        ) { request ->
            val args = request.params.arguments ?: error("No arguments")
            val previewId = args["previewId"]?.jsonPrimitive?.content ?: error("Missing previewId")

            val state = states[previewId]
            if (state == null) {
                CallToolResult(
                    content = listOf(
                        TextContent(
                            text = "Preview '$previewId' not found. Available previews: ${states.keys}",
                        ),
                    ),
                )
            } else {
                state.onClearEvents()
                CallToolResult(
                    content = listOf(
                        TextContent(
                            text = "All events cleared successfully.",
                        ),
                    ),
                )
            }
        }
    }

    /**
     * Extract previewId from URI query parameter.
     * Expected format: .../resource?previewId=xxx
     */
    private fun extractPreviewId(uri: String): String? {
        val queryStart = uri.indexOf('?')
        if (queryStart == -1) return null

        val query = uri.substring(queryStart + 1)
        return query.split('&')
            .map { it.split('=', limit = 2) }
            .find { it.size == 2 && it[0] == "previewId" }
            ?.get(1)
            ?.let { java.net.URLDecoder.decode(it, Charsets.UTF_8.name()) }
    }
}

/**
 * Encodes an ImageBitmap to a base64-encoded PNG string.
 */
private fun ImageBitmap.encodeToBase64Png(): String {
    val data = Image
        .makeFromBitmap(this.asSkiaBitmap())
        .encodeToData(EncodedImageFormat.PNG, 100)
        ?: throw IllegalStateException("Failed to encode ImageBitmap to PNG")
    return Base64.getEncoder().encodeToString(data.bytes)
}

/**
 * Converts a Map to a JsonElement.
 */
private fun Map<String, Any?>.toJsonElement(): JsonElement = buildJsonObject {
    forEach { (key, value) ->
        when (value) {
            is String -> put(key, JsonPrimitive(value))
            is Boolean -> put(key, JsonPrimitive(value))
            is Number -> put(key, JsonPrimitive(value))
            is List<*> -> put(
                key,
                JsonArray(
                    value.map { item ->
                        when (item) {
                            is String -> JsonPrimitive(item)
                            else -> JsonPrimitive(item.toString())
                        }
                    },
                ),
            )
            null -> put(key, kotlinx.serialization.json.JsonNull)
            else -> put(key, JsonPrimitive(value.toString()))
        }
    }
}
