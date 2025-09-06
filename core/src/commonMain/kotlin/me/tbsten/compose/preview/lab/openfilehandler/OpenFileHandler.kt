package me.tbsten.compose.preview.lab.openfilehandler

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler

/**
 * interface, which determines the behavior when opening source code, etc.
 * PreviewLabRoot に指定することで機能するようになります。
 *
 * ```kt
 * PreviewLabRoot(
 *   openFileHandler = object : OpenFileHandler<UriHandler> { ... }
 * )
 * ```
 *
 * @param T Type of value that must be configured in the Composable function.
 */
interface OpenFileHandler<T> {
    /**
     * If there is data that needs to be prepared in the Composable function, specify it here.
     */
    @Composable
    fun configure(): T

    /**
     * Open a file in the project.
     *
     * @param params A class that contains values and file information set in [configure].
     */
    fun openFile(params: Params<T>)
    class Params<T> internal constructor(val configuredValue: T, val filePathInProject: String, val startLineNumber: Int?)
}

/**
 * Function to easily create an OpenFileHandler implementation.
 * @param openFile Function that actually opens the file.
 */
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

/**
 * OpenFileHandler, which opens a file based on a base URL.
 * Generate a URL in the format `$baseUrl${filePathInProject}${startLineNumberがある場合は "#L${startLineNumber}"}` and open the URL using the platform default method (use LocalUriHandler).
 *
 * For example, to open source code on Github, you can configure the following
 *
 * ```kt
 * val githubOpenFileHandler = UrlOpenFileHandler("https://github.com/me/my-repo/blob/main/")
 * ```
 */
open class UrlOpenFileHandler(private val baseUrl: String) : OpenFileHandler<UriHandler> {
    @Composable
    override fun configure(): UriHandler = LocalUriHandler.current

    override fun openFile(params: OpenFileHandler.Params<UriHandler>) {
        params.configuredValue.openUri(
            "$baseUrl${params.filePathInProject}${if (params.startLineNumber != null) "#L${params.startLineNumber}" else ""}",
        )
    }
}

internal val LocalOpenFileHandler = compositionLocalOf<OpenFileHandler<out Any?>?> { null }

class GithubOpenFileHandler(githubRepository: String, branch: String = "main", server: String = "https://github.com") :
    UrlOpenFileHandler(baseUrl = "$server/$githubRepository/blob/$branch/")
