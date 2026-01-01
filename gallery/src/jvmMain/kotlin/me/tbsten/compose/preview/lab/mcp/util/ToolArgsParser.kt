package me.tbsten.compose.preview.lab.mcp.util

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Exception thrown when one or more tool arguments are invalid.
 * Collects all validation errors and reports them together.
 */
class ToolArgsValidationException(val errors: List<String>) :
    Exception(
        "Tool argument validation failed:\n${errors.joinToString("\n") { "- $it" }}",
    )

/**
 * Parser for MCP tool arguments with soft assertion-style validation.
 * Collects all errors and reports them together instead of failing on the first error.
 *
 * Usage:
 * ```
 * val parser = ToolArgsParser(args)
 * val previewId = parser.requireString("previewId")
 * val label = parser.requireString("label")
 * val index = parser.requireInt("index")
 * parser.validate() // throws if any errors occurred
 * ```
 */
class ToolArgsParser(private val args: JsonObject?) {
    private val errors = mutableListOf<String>()
    private val parsedValues = mutableMapOf<String, Any?>()

    init {
        if (args == null) {
            errors.add("Missing arguments")
        }
    }

    /**
     * Requires a string argument.
     * Returns the value if present, or an empty string if missing (error is collected).
     */
    fun requireString(name: String, description: String? = null): String {
        if (args == null) return ""
        val value = args[name]?.jsonPrimitive?.content
        if (value == null) {
            val desc = description ?: name
            errors.add("Missing required argument: $desc")
            return ""
        }
        parsedValues[name] = value
        return value
    }

    /**
     * Gets an optional string argument with a default value.
     */
    fun optionalString(name: String, default: String): String {
        if (args == null) return default
        return args[name]?.jsonPrimitive?.content ?: default
    }

    /**
     * Requires an integer argument.
     * Returns the value if present and valid, or 0 if missing/invalid (error is collected).
     */
    fun requireInt(name: String, description: String? = null): Int {
        if (args == null) return 0
        val rawValue = args[name]?.jsonPrimitive?.content
        if (rawValue == null) {
            val desc = description ?: name
            errors.add("Missing required argument: $desc")
            return 0
        }
        val value = rawValue.toIntOrNull()
        if (value == null) {
            val desc = description ?: name
            errors.add("Invalid integer value for '$desc': $rawValue")
            return 0
        }
        parsedValues[name] = value
        return value
    }

    /**
     * Gets an optional integer argument with a default value.
     */
    fun optionalInt(name: String, default: Int): Int {
        if (args == null) return default
        return args[name]?.jsonPrimitive?.content?.toIntOrNull() ?: default
    }

    /**
     * Gets the raw JsonElement for a given argument name.
     */
    fun getRaw(name: String): JsonElement? = args?.get(name)

    /**
     * Returns true if there are any validation errors.
     */
    fun hasErrors(): Boolean = errors.isNotEmpty()

    /**
     * Returns the list of collected errors.
     */
    fun getErrors(): List<String> = errors.toList()

    /**
     * Adds a custom error message.
     */
    fun addError(message: String) {
        errors.add(message)
    }

    /**
     * Validates all collected arguments.
     * Throws [ToolArgsValidationException] if any errors were collected.
     */
    fun validate() {
        if (errors.isNotEmpty()) {
            throw ToolArgsValidationException(errors)
        }
    }

    /**
     * Returns an error message string if there are errors, or null if validation passed.
     * Useful for returning error results without throwing exceptions.
     */
    fun errorMessageOrNull(): String? {
        if (errors.isEmpty()) return null
        return "Argument validation failed:\n${errors.joinToString("\n") { "- $it" }}"
    }
}

/**
 * Creates a ToolArgsParser and runs the validation block.
 * Returns the error message if validation failed, or null if successful.
 */
inline fun parseToolArgs(args: JsonObject?, block: ToolArgsParser.() -> Unit): ToolArgsParser {
    val parser = ToolArgsParser(args)
    parser.block()
    return parser
}
