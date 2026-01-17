package me.tbsten.compose.preview.lab.mcp.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
internal val json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    isLenient = false
    coerceInputValues = false
    allowComments = true
    allowTrailingComma = true
    allowStructuredMapKeys = true
    allowSpecialFloatingPointValues = true
}
