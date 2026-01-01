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
import java.io.File
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonObject
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.MutablePreviewLabField
import me.tbsten.compose.preview.lab.PreviewLabEvent
import me.tbsten.compose.preview.lab.PreviewLabField
import me.tbsten.compose.preview.lab.mcp.util.ToolArgsParser
import me.tbsten.compose.preview.lab.mcp.util.json
import me.tbsten.compose.preview.lab.mcp.util.notifyResourceListChanged
import me.tbsten.compose.preview.lab.mcp.util.notifyResourceUpdated
import me.tbsten.compose.preview.lab.mcp.util.notifyToolListChanged
import me.tbsten.compose.preview.lab.mcp.util.putResource
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

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
internal class PreviewLabMcpStateManager(private val server: Server, private val scope: CoroutineScope,) {
    private val states = ConcurrentHashMap<String, PreviewLabMcpState>()
    private var toolsRegistered = false
    private var listResourceRegistered = false

    fun updateState(state: PreviewLabMcpState) {
        val isNewPreview = !states.containsKey(state.previewId)
        states[state.previewId] = state
        ensureToolsRegistered()
        ensureListResourceRegistered()
        if (isNewPreview) {
            registerPreviewResources(state.previewId)
            server.notifyResourceListChanged(scope)
        } else {
            // Existing preview updated - notify resource updated
            val baseUrl = "$McpBaseUrl/preview-lab"
            server.notifyResourceUpdated("$baseUrl/${state.previewId}/fields", scope)
            server.notifyResourceUpdated("$baseUrl/${state.previewId}/events", scope)
        }
    }

    fun removeState(previewId: String) {
        states.remove(previewId)
        unregisterPreviewResources(previewId)
        server.notifyResourceListChanged(scope)
    }

    fun getState(previewId: String): PreviewLabMcpState? = states[previewId]

    fun getAllStates(): Map<String, PreviewLabMcpState> = states.toMap()

    @OptIn(ExperimentalComposePreviewLabApi::class)
    private fun ensureToolsRegistered() {
        if (toolsRegistered) return
        toolsRegistered = true
        registerTools()
        server.notifyToolListChanged(scope)
    }

    private fun ensureListResourceRegistered() {
        if (listResourceRegistered) return
        listResourceRegistered = true
        registerListResource()
        server.notifyResourceListChanged(scope)
    }

    private fun registerListResource() {
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
    }

    @OptIn(ExperimentalComposePreviewLabApi::class, ExperimentalTime::class)
    private fun registerPreviewResources(previewId: String) {
        val baseUrl = "$McpBaseUrl/preview-lab"
        val fieldsUri = "$baseUrl/$previewId/fields"
        val eventsUri = "$baseUrl/$previewId/events"

        // Resource: Get fields for a specific preview
        server.putResource(
            uri = fieldsUri,
            name = "PreviewLab fields: $previewId",
            description = """
                Get the list of fields for PreviewLab instance '$previewId'.
                Each field includes: label, value (toString), and whether it is mutable.
            """.trimIndent(),
            mimeType = "application/json",
        ) {
            val state = states[previewId]
            if (state == null) {
                ReadResourceResult(
                    contents = listOf(
                        TextResourceContents(
                            uri = fieldsUri,
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
                            uri = fieldsUri,
                            text = json.encodeToString(
                                JsonArray(
                                    state.fields.map { field ->
                                        buildJsonObject {
                                            put("label", JsonPrimitive(field.label))
                                            put("value", JsonPrimitive(field.value.toString()))
                                            put("isMutable", JsonPrimitive(field is MutablePreviewLabField<*>))
                                        }
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
            uri = eventsUri,
            name = "PreviewLab events: $previewId",
            description = """
                Get the list of events for PreviewLab instance '$previewId'.
                Each event includes: title, description, and timestamp.
            """.trimIndent(),
            mimeType = "application/json",
        ) {
            val state = states[previewId]
            if (state == null) {
                ReadResourceResult(
                    contents = listOf(
                        TextResourceContents(
                            uri = eventsUri,
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
                            uri = eventsUri,
                            text = json.encodeToString(
                                JsonArray(
                                    state.events.map { event ->
                                        buildJsonObject {
                                            put("title", JsonPrimitive(event.title))
                                            put("description", JsonPrimitive(event.description ?: ""))
                                            put("createdAt", JsonPrimitive(event.createAt.toString()))
                                        }
                                    },
                                ),
                            ),
                        ),
                    ),
                )
            }
        }
    }

    private fun unregisterPreviewResources(previewId: String) {
        val baseUrl = "$McpBaseUrl/preview-lab"
        server.removeResource("$baseUrl/$previewId/fields")
        server.removeResource("$baseUrl/$previewId/events")
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
            val parser = ToolArgsParser(request.params.arguments)
            val previewId = parser.requireString("previewId", "preview ID")
            val label = parser.requireString("label", "field label")
            val serializedValue = parser.requireString("serializedValue", "serialized value")
            parser.errorMessageOrNull()?.let { errorMessage ->
                return@addTool CallToolResult(content = listOf(TextContent(text = errorMessage)))
            }

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
            val parser = ToolArgsParser(request.params.arguments)
            val previewId = parser.requireString("previewId", "preview ID")
            val label = parser.requireString("label", "field label")
            val testValueIndex = parser.requireInt("testValueIndex", "test value index")
            parser.errorMessageOrNull()?.let { errorMessage ->
                return@addTool CallToolResult(content = listOf(TextContent(text = errorMessage)))
            }

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
                Returns the screenshot as a PNG image, or saves it to a file if outputPath is specified.
            """.trimIndent(),
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    putJsonObject("previewId") {
                        put("type", JsonPrimitive("string"))
                        put("description", JsonPrimitive("The ID of the preview to capture."))
                    }
                    putJsonObject("outputPath") {
                        put("type", JsonPrimitive("string"))
                        put(
                            "description",
                            JsonPrimitive(
                                "Optional file path to save the screenshot. " +
                                    "If specified, the screenshot will be saved as a PNG file. " +
                                    "If not specified, the screenshot will be returned as base64 image data.",
                            ),
                        )
                    }
                },
                required = listOf("previewId"),
            ),
        ) { request ->
            val parser = ToolArgsParser(request.params.arguments)
            val previewId = parser.requireString("previewId", "preview ID")
            val outputPath = parser.optionalString("outputPath", "")
                .ifEmpty { null }
            parser.errorMessageOrNull()?.let { errorMessage ->
                return@addTool CallToolResult(content = listOf(TextContent(text = errorMessage)))
            }

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
                val imageBitmap = state.captureScreenshot()
                if (imageBitmap != null) {
                    if (outputPath != null) {
                        // Save to file
                        val pngData = imageBitmap.encodeToPngBytes()
                        val file = File(outputPath)
                        file.parentFile?.mkdirs()
                        file.writeBytes(pngData)
                        CallToolResult(
                            content = listOf(
                                TextContent(
                                    text = "Screenshot saved to: ${file.absolutePath}",
                                ),
                            ),
                        )
                    } else {
                        // Return as base64
                        val base64Image = imageBitmap.encodeToBase64Png()
                        CallToolResult(
                            content = listOf(
                                ImageContent(
                                    data = base64Image,
                                    mimeType = "image/png",
                                ),
                            ),
                        )
                    }
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
            val parser = ToolArgsParser(request.params.arguments)
            val previewId = parser.requireString("previewId", "preview ID")
            parser.errorMessageOrNull()?.let { errorMessage ->
                return@addTool CallToolResult(content = listOf(TextContent(text = errorMessage)))
            }

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
}

/**
 * Encodes an ImageBitmap to PNG byte array.
 */
private fun ImageBitmap.encodeToPngBytes(): ByteArray {
    val data = Image
        .makeFromBitmap(this.asSkiaBitmap())
        .encodeToData(EncodedImageFormat.PNG, 100)
        ?: throw IllegalStateException("Failed to encode ImageBitmap to PNG")
    return data.bytes
}

/**
 * Encodes an ImageBitmap to a base64-encoded PNG string.
 */
private fun ImageBitmap.encodeToBase64Png(): String = Base64.getEncoder().encodeToString(encodeToPngBytes())
