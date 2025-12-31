package me.tbsten.compose.preview.lab.mcp

import io.ktor.server.engine.EmbeddedServer
import io.modelcontextprotocol.kotlin.sdk.server.Server as McpServer

class PreviewLabMcpServer internal constructor(
    private val config: PreviewLabMcpServerConfig,
    private val ktorServer: EmbeddedServer<*, *>,
    private val mcpServer: McpServer,
) {
    fun start() {
        ktorServer.start(wait = false)
    }

    fun stop() {
        ktorServer.stop(timeoutMillis = 5_000)
    }
}
