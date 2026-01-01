package me.tbsten.compose.preview.lab.mcp

import io.modelcontextprotocol.kotlin.sdk.server.Server

open class PreviewLabMcpServerConfig(
    val enabled: Boolean = true,
    val host: String = "0.0.0.0",
    val port: Int = 7007,
    val additionalMcpServerSetting: Server.() -> Unit = {},
) {
    data object Disable : PreviewLabMcpServerConfig(enabled = false)
}

internal const val McpBaseUrl = "preview-lab:///"
