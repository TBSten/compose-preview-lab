package me.tbsten.compose.preview.lab.mcp

internal fun printStartMcpServerMessage(config: PreviewLabMcpServerConfig) = with(config) {
    println("==================================================")
    println("=                                                =")
    println("=  🔍 Compose Preview Lab MCP Server             =")
    println("=     start on http://$host:$port               =")
    println("=                                                =")
    println("==================================================")
}

internal fun printFailStartMcpServerMessage(config: PreviewLabMcpServerConfig, error: Throwable?) = with(config) {
    println("⚠️ WARN: ")
    println("⚠️ WARN: Compose Preview Lab MCP Server failed to start.")
    error?.let { println("⚠️ WARN: (${error.message})") }
    println("⚠️ WARN:   - URL: http://$host:$port")
    if (error == null) {
        println("⚠️ WARN:   (No throwable)")
    } else {
        println("⚠️ WARN:   Error stacktrace")
        println(
            error
                .stackTraceToString()
                .prependIndent("⚠️ WARN:       "),
        )
    }
}
