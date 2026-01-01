package me.tbsten.compose.preview.lab.mcp

import androidx.compose.runtime.compositionLocalOf
import io.ktor.server.engine.EmbeddedServer
import io.modelcontextprotocol.kotlin.sdk.server.Server as McpServer

class PreviewLabMcpServer internal constructor(
    internal val config: PreviewLabMcpServerConfig,
    internal val ktorServer: EmbeddedServer<*, *>,
    internal val mcpServer: McpServer,
) {
    fun start() {
        ktorServer.start(wait = false)
    }

    fun stop() {
        ktorServer.stop(timeoutMillis = 5_000)
    }
}

val LocalPreviewLabMcpServer = compositionLocalOf<PreviewLabMcpServer?> { null }
