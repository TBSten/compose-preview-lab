package me.tbsten.compose.preview.lab.mcp

import androidx.compose.runtime.compositionLocalOf
import io.ktor.server.engine.EmbeddedServer
import io.modelcontextprotocol.kotlin.sdk.server.Server as McpServer
import me.tbsten.compose.preview.lab.gallery.PreviewLabGalleryState

class PreviewLabMcpServer internal constructor(
    internal val config: PreviewLabMcpServerConfig,
    internal val ktorServer: EmbeddedServer<*, *>,
    internal val mcpServer: McpServer,
) {
    fun start() {
        ktorServer.start(wait = false)
    }

    fun updateState(state: PreviewLabGalleryState) {
        /* TODO */
    }

    fun stop() {
        ktorServer.stop(timeoutMillis = 5_000)
    }
}

val LocalPreviewLabMcpServer = compositionLocalOf<PreviewLabMcpServer?> { null }
