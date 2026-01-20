package me.tbsten.compose.preview.lab.previewlab.openfilehandler

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
import me.tbsten.compose.preview.lab.util.JsOnlyExport

/**
 * interface, which determines the behavior when opening source code, etc.
 * PreviewLabGallery に指定することで機能するようになります。
 *
 * ```kt
 * PreviewLabGallery(
 *   openFileHandler = object : OpenFileHandler<UriHandler> { ... }
 * )
 * ```
 *
 * @param T Type of value that must be configured in the Composable function.
 */
@OptIn(ExperimentalJsExport::class)
@JsOnlyExport
public interface OpenFileHandler<T> {
    /**
     * If there is data that needs to be prepared in the Composable function, specify it here.
     */
    @JsExport.Ignore
    @Composable
    public fun configure(): T

    /**
     * Open a file in the project.
     *
     * @param params A class that contains values and file information set in [configure].
     */
    @JsExport.Ignore
    public fun openFile(params: Params<T>)

    @JsExport.Ignore
    public class Params<T> internal constructor(
        public val configuredValue: T,
        public val filePathInProject: String,
        public val startLineNumber: Int?
    )
}

/**
 * Function to easily create an OpenFileHandler implementation.
 * @param openFile Function that actually opens the file.
 */
@Suppress("ktlint:standard:function-naming")
public fun OpenFileHandler(openFile: (OpenFileHandler.Params<Unit>) -> Unit): OpenFileHandler<Unit> =
    object : OpenFileHandler<Unit> {
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
public open class UrlOpenFileHandler(private val baseUrl: String) : OpenFileHandler<UriHandler> {
    @Composable
    override fun configure(): UriHandler = LocalUriHandler.current

    override fun openFile(params: OpenFileHandler.Params<UriHandler>) {
        params.configuredValue.openUri(
            "$baseUrl${params.filePathInProject}${if (params.startLineNumber != null) "#L${params.startLineNumber}" else ""}",
        )
    }
}

/**
 * **This is an internal annotation for Compose Preview Lab. Don't use this api manually.**
 */
@InternalComposePreviewLabApi
public val LocalOpenFileHandler: androidx.compose.runtime.ProvidableCompositionLocal<OpenFileHandler<out Any?>?> =
    compositionLocalOf<OpenFileHandler<out Any?>?> { null }

/**
 * An OpenFileHandler that opens files in GitHub's web interface.
 *
 * This handler generates URLs that point to specific files and line numbers in a GitHub repository,
 * allowing users to view the source code of previews directly in their web browser.
 *
 * @param githubRepository The GitHub repository in format "owner/repository"
 * @param branch The branch name to link to (defaults to "main")
 * @param server The GitHub server URL (defaults to "https://github.com" for public GitHub)
 */
public class GithubOpenFileHandler(githubRepository: String, branch: String = "main", server: String = "https://github.com") :
    UrlOpenFileHandler(baseUrl = "$server/$githubRepository/blob/$branch/")
