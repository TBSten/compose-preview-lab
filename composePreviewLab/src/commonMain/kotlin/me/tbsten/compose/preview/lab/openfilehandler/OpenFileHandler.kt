package me.tbsten.compose.preview.lab.openfilehandler

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler

interface OpenFileHandler<T> {
    @Composable
    fun configure(): T
    fun openFile(configuredValue: T, filePathInProject: String)
}

@Suppress("ktlint:standard:function-naming")
fun OpenFileHandler(openFile: (filePathInProject: String) -> Unit) = object : OpenFileHandler<Unit> {
    @Composable
    override fun configure() {
    }

    override fun openFile(configuredValue: Unit, filePathInProject: String) = openFile(filePathInProject)
}

class UrlOpenFileHandler(private val baseUrl: String = "") : OpenFileHandler<UriHandler> {
    @Composable
    override fun configure(): UriHandler = LocalUriHandler.current

    override fun openFile(configuredValue: UriHandler, filePathInProject: String) {
        configuredValue.openUri("$baseUrl$filePathInProject")
    }
}

internal val LocalOpenFileHandler = compositionLocalOf<OpenFileHandler<out Any?>?> { null }
