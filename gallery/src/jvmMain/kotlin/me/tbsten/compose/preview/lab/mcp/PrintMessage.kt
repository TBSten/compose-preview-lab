package me.tbsten.compose.preview.lab.mcp

internal fun printStartMcpServerMessage(config: PreviewLabMcpServerConfig) = with(config) {
    println(
        """
            ====================================================
            ==                                                ==
            ==  🔍 Compose Preview Lab MCP Server             ==
            ==     start on http://$host:$port               ==
            ==                                                ==
            ====================================================
        """.trimIndent(),
    )
}

internal fun printFailStartMcpServerMessage(config: PreviewLabMcpServerConfig, error: Throwable?) = with(config) {
    println(
        buildString {
            appendLine("⚠️ WARN: ")
            appendLine("⚠️ WARN: Compose Preview Lab MCP Server failed to start.")
            error?.let { appendLine("⚠️ WARN: (${error.message})") }
            appendLine("⚠️ WARN:   - URL: http://$host:$port")
            if (error == null) {
                appendLine("⚠️ WARN:   (No throwable)")
            } else {
                appendLine("⚠️ WARN:   Error stacktrace")
                appendLine(
                    error
                        .stackTraceToString()
                        .prependIndent("⚠️ WARN:       "),
                )
            }
        },
    )
}
