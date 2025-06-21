package me.tbsten.compose.preview.lab.openfilehandler

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler

interface OpenFileHandler<T> {
    @Composable
    fun configure(): T
    fun openFile(params: Params<T>)
    class Params<T> internal constructor(val configuredValue: T, val filePathInProject: String, val startLineNumber: Int?,)
}

@Suppress("ktlint:standard:function-naming")
fun OpenFileHandler(openFile: (OpenFileHandler.Params<Unit>) -> Unit) = object : OpenFileHandler<Unit> {
    @Composable
    override fun configure() {
    }

    override fun openFile(params: OpenFileHandler.Params<Unit>) = openFile(
        OpenFileHandler.Params(
            configuredValue = Unit,
            filePathInProject = params.filePathInProject,
            startLineNumber = params.startLineNumber,
        ),
    )
}

class UrlOpenFileHandler(private val baseUrl: String = "") : OpenFileHandler<UriHandler> {
    @Composable
    override fun configure(): UriHandler = LocalUriHandler.current

    override fun openFile(params: OpenFileHandler.Params<UriHandler>) {
        params.configuredValue.openUri("$baseUrl${params.filePathInProject}${if (params.startLineNumber != null) "#L${params.startLineNumber}" else ""}")
    }
}

internal val LocalOpenFileHandler = compositionLocalOf<OpenFileHandler<out Any?>?> { null }
