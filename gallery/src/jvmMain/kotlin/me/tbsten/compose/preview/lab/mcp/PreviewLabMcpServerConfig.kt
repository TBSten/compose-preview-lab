package me.tbsten.compose.preview.lab.mcp

import io.modelcontextprotocol.kotlin.sdk.server.Server

/**
 * Configuration for the MCP (Model Context Protocol) server.
 *
 * **Note: MCP Server is an experimental feature.** The API may change in future versions.
 *
 * This class configures how the MCP server runs within PreviewLabGallery,
 * allowing AI assistants (Claude Code, Claude Desktop, Cursor, etc.)
 * to interact with previews.
 *
 * ```kotlin
 * // Default configuration (enabled on port 7007)
 * PreviewLabGalleryWindows(
 *     previewList = myModule.PreviewList,
 *     mcpServerConfig = PreviewLabMcpServerConfig(),
 * )
 *
 * // Custom port
 * PreviewLabGalleryWindows(
 *     previewList = myModule.PreviewList,
 *     mcpServerConfig = PreviewLabMcpServerConfig(port = 8080),
 * )
 *
 * // Disable MCP server
 * PreviewLabGalleryWindows(
 *     previewList = myModule.PreviewList,
 *     mcpServerConfig = PreviewLabMcpServerConfig.Disable,
 * )
 * ```
 *
 * @property enabled Whether the MCP server is enabled. Defaults to `true`.
 * @property host The host address to bind the server to. Defaults to `"0.0.0.0"`.
 * @property port The port number for the MCP server. Defaults to `7007`.
 * @property additionalMcpServerSetting Additional configuration block for the MCP server.
 * @see PreviewLabGalleryWindows
 */
public open class PreviewLabMcpServerConfig(
    public val enabled: Boolean = true,
    public val host: String = "0.0.0.0",
    public val port: Int = 7007,
    public val additionalMcpServerSetting: Server.() -> Unit = {},
) {
    /**
     * Predefined configuration to disable the MCP server.
     */
    public data object Disable : PreviewLabMcpServerConfig(enabled = false)
}

internal const val McpBaseUrl = "preview-lab://"
