package me.tbsten.compose.preview.lab.mcp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ReadResourceResult
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities.Prompts
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities.Resources
import io.modelcontextprotocol.kotlin.sdk.types.TextResourceContents

@Composable
internal fun ProvideMcpServer(
    mcpServerConfig: PreviewLabMcpServerConfig = PreviewLabMcpServerConfig(),
    content: @Composable () -> Unit,
) {
    var server by remember { mutableStateOf<PreviewLabMcpServer?>(null) }

    DisposableEffect(mcpServerConfig) {
        server = startPreviewLabMcpServer(mcpServerConfig)

        onDispose {
            server?.stop()
        }
    }

    // TODO Provide
    content()
}

private fun startPreviewLabMcpServer(config: PreviewLabMcpServerConfig): PreviewLabMcpServer? = runCatching {
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

        PreviewLabMcpServer(
            config = config,
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
            prompts = Prompts(listChanged = false),
            resources = Resources(subscribe = true, listChanged = true),
        ),
    ),
) {
    "TODO ".repeat(20)
}.apply(Server::applyDefaultPreviewLabServerSetting).apply {
    config.additionalMcpServerSetting(this)
}

private fun Server.applyDefaultPreviewLabServerSetting() {
    addResource(
        uri = "$McpBaseUrl/example",
        name = "Example Resource",
        description = """TODO """.repeat(10),
        mimeType = "text/plain",
    ) { request ->
        ReadResourceResult(
            contents = listOf(
                TextResourceContents(
                    text = "meta=${request.meta?.json}",
                    uri = request.uri,
                    mimeType = "text/plain",
                ),
            ),
        )
    }
}
