package me.tbsten.compose.preview.lab.mcp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities.Prompts
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities.Resources
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities.Tools
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import me.tbsten.compose.preview.lab.PreviewLabPreview
import me.tbsten.compose.preview.lab.gallery.PreviewLabGalleryState

private class McpServerHolder(
    val ktorServer: EmbeddedServer<*, *>,
    val mcpServer: Server,
) {
    fun start() {
        ktorServer.start(wait = false)
    }

    fun stop() {
        ktorServer.stop(timeoutMillis = 5_000)
    }
}

@Composable
internal fun ProvideMcpServer(
    previewList: List<PreviewLabPreview>?,
    featuredFileList: Map<String, List<String>>?,
    state: PreviewLabGalleryState?,
    config: PreviewLabMcpServerConfig = PreviewLabMcpServerConfig(),
    content: @Composable () -> Unit,
) {
    var holder by remember { mutableStateOf<McpServerHolder?>(null) }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(config) {
        holder = startMcpServer(config)
        coroutineScope.launch {
            previewList?.let { previewList ->
                snapshotFlow { previewList }
                    .distinctUntilChanged()
                    .collect { holder?.mcpServer?.updatePreviewList(it) }
            }
        }

        coroutineScope.launch {
            featuredFileList?.let { featuredFileList ->
                snapshotFlow { featuredFileList }
                    .distinctUntilChanged()
                    .collect { holder?.mcpServer?.updateFeaturedFileList(it) }
            }
        }

        coroutineScope.launch {
            state?.let { state ->
                previewList?.let { previewList ->
                    snapshotFlow { state to previewList }
                        .distinctUntilChanged()
                        .collect { holder?.mcpServer?.updateState(it.first, it.second) }
                }
            }
        }

        onDispose {
            holder?.stop()
        }
    }

    content()
}

private fun startMcpServer(config: PreviewLabMcpServerConfig): McpServerHolder? = runCatching {
    if (config.enabled) {
        val mcpServer = createMcpServer(config)

        val ktorServer =
            embeddedServer(
                CIO,
                host = config.host,
                port = config.port,
            ) {
                mcp { mcpServer }
            }

        McpServerHolder(
            ktorServer = ktorServer,
            mcpServer = mcpServer,
        ).also {
            it.start()
        }
    } else {
        null
    }
}.also {
    if (it.isSuccess && it.getOrNull() != null) {
        printStartMcpServerMessage(config)
    } else if (it.isFailure) {
        printFailStartMcpServerMessage(config, it.exceptionOrNull())
    }
}.getOrNull()

private fun createMcpServer(config: PreviewLabMcpServerConfig): Server = Server(
    serverInfo = Implementation(
        name = "Compose Preview Lab MCP (SSE)",
        version = "0.1.0-dev09", // TODO
    ),
    options = ServerOptions(
        capabilities = ServerCapabilities(
            prompts = Prompts(listChanged = true),
            resources = Resources(subscribe = true, listChanged = true),
            tools = Tools(listChanged = true),
        ),
    ),
) {
    "TODO ".repeat(20)
}.apply(Server::applyDefaultPreviewLabServerSetting).apply {
    config.additionalMcpServerSetting(this)
}

private fun Server.applyDefaultPreviewLabServerSetting() {
}
