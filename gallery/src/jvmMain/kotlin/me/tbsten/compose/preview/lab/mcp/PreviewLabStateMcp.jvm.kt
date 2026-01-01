package me.tbsten.compose.preview.lab.mcp

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.ReadResourceResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.TextResourceContents
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import io.modelcontextprotocol.kotlin.sdk.types.toJson
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.putJsonObject
import kotlin.time.ExperimentalTime
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.MutablePreviewLabField
import me.tbsten.compose.preview.lab.PreviewLabEvent
import me.tbsten.compose.preview.lab.PreviewLabField
import me.tbsten.compose.preview.lab.mcp.util.json
import me.tbsten.compose.preview.lab.mcp.util.putResource
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import java.util.Base64

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
 * Updates MCP resources and tools for PreviewLab state.
 */
@OptIn(ExperimentalComposePreviewLabApi::class, ExperimentalTime::class)
internal fun Server.updatePreviewLabState(state: PreviewLabMcpState) {
    val previewId = state.previewId
    val baseUrl = "$McpBaseUrl/preview-lab/$previewId"

    // Resource: Field list (summary)
    putResource(
        uri = "$baseUrl/fields",
        name = "PreviewLab fields ($previewId)",
        description = """
            List of all fields in PreviewLab instance '$previewId'.
            Each field includes: label, value (toString), and whether it is mutable.
        """.trimIndent(),
        mimeType = "application/json",
    ) {
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

    // Resource: Individual field details
    state.fields.forEach { field ->
        val fieldLabel = field.label
        val fieldUrl = "$baseUrl/fields/${encodeFieldLabel(fieldLabel)}"

        putResource(
            uri = fieldUrl,
            name = "Field: $fieldLabel ($previewId)",
            description = """
                Detailed information about field '$fieldLabel' in PreviewLab instance '$previewId'.
                Includes: label, value, valueCode, testValues, serializedValue, and fieldType.
            """.trimIndent(),
            mimeType = "application/json",
        ) {
            ReadResourceResult(
                contents = listOf(
                    TextResourceContents(
                        uri = fieldUrl,
                        text = serializeFieldDetails(field),
                    ),
                ),
            )
        }
    }

    // Resource: Events list
    putResource(
        uri = "$baseUrl/events",
        name = "PreviewLab events ($previewId)",
        description = """
            List of all events recorded in PreviewLab instance '$previewId'.
            Each event includes: title, description, and timestamp.
        """.trimIndent(),
        mimeType = "application/json",
    ) {
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

    // Tool: Update field value
    addTool(
        name = "Update PreviewLab field ($previewId)",
        description = """
            Update a field value in PreviewLab instance '$previewId'.
            The value should be provided as a JSON-serialized string that matches the field's serializer.
        """.trimIndent(),
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("label") {
                    put("type", JsonPrimitive("string"))
                    put("description", JsonPrimitive("The label of the field to update."))
                }
                putJsonObject("serializedValue") {
                    put("type", JsonPrimitive("string"))
                    put("description", JsonPrimitive("The new value as a JSON-serialized string."))
                }
            },
            required = listOf("label", "serializedValue"),
        ),
    ) { request ->
        val args = request.params.arguments ?: error("No arguments")
        val label = args["label"]?.jsonPrimitive?.content ?: error("Missing label")
        val serializedValue = args["serializedValue"]?.jsonPrimitive?.content ?: error("Missing serializedValue")

        val success = state.onUpdateField(label, serializedValue)
        CallToolResult(
            content = listOf(
                TextContent(
                    text = if (success) {
                        "Field '$label' updated successfully."
                    } else {
                        "Failed to update field '$label'. The field may not exist, may not be mutable, or the value format may be invalid."
                    },
                ),
            ),
        )
    }

    // Tool: Take screenshot
    addTool(
        name = "Take PreviewLab screenshot ($previewId)",
        description = """
            Capture a screenshot of the preview content in PreviewLab instance '$previewId'.
            Returns the screenshot as a base64-encoded PNG image.
        """.trimIndent(),
        inputSchema = ToolSchema(
            properties = buildJsonObject { },
            required = emptyList(),
        ),
    ) {
        val imageBitmap = runBlocking { state.captureScreenshot() }
        if (imageBitmap != null) {
            val base64Image = imageBitmap.encodeToBase64Png()
            CallToolResult(
                content = listOf(
                    TextContent(
                        text = "Screenshot captured successfully. Base64 PNG data:\n$base64Image",
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

    // Tool: Clear all events
    addTool(
        name = "Clear PreviewLab events ($previewId)",
        description = """
            Clear all recorded events in PreviewLab instance '$previewId'.
        """.trimIndent(),
        inputSchema = ToolSchema(
            properties = buildJsonObject { },
            required = emptyList(),
        ),
    ) {
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

/**
 * Removes MCP resources and tools for a PreviewLab instance.
 */
internal fun Server.removePreviewLabState(previewId: String) {
    val baseUrl = "$McpBaseUrl/preview-lab/$previewId"

    removeResource("$baseUrl/fields")
    removeResource("$baseUrl/events")

    removeTool("Update PreviewLab field ($previewId)")
    removeTool("Take PreviewLab screenshot ($previewId)")
    removeTool("Clear PreviewLab events ($previewId)")
}

/**
 * Serializes field details to JSON string.
 */
@Suppress("UNCHECKED_CAST")
private fun <T> serializeFieldDetails(field: PreviewLabField<T>): String {
    val serializer = field.serializer()
    val serializedValue = if (serializer != null) {
        try {
            json.encodeToString(serializer, field.value)
        } catch (e: Exception) {
            null
        }
    } else {
        null
    }

    return json.encodeToString(
        mapOf(
            "label" to field.label,
            "value" to field.value.toString(),
            "valueCode" to field.valueCode(),
            "testValues" to field.testValues().map { it.toString() },
            "serializedValue" to serializedValue,
            "fieldType" to field::class.qualifiedName,
            "isMutable" to (field is MutablePreviewLabField<*>),
        ).toJson(),
    )
}

/**
 * Encodes a field label for use in URLs.
 */
private fun encodeFieldLabel(label: String): String = java.net.URLEncoder.encode(label, Charsets.UTF_8.name())

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
                    }
                )
            )
            null -> put(key, kotlinx.serialization.json.JsonNull)
            else -> put(key, JsonPrimitive(value.toString()))
        }
    }
}
