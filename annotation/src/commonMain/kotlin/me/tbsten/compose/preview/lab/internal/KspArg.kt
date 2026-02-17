package me.tbsten.compose.preview.lab.internal

import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi

/**
 * **This is an internal object for Compose Preview Lab. Don't use this api manually.**
 *
 * Keys for Kotlin Compiler Plugin arguments passed from the Gradle plugin.
 */
@Suppress("ktlint:standard:property-naming")
@InternalComposePreviewLabApi
object CompilerArg {
    private const val PREFIX = "composePreviewLab."
    const val previewsListPackage = "${PREFIX}previewsListPackage"
    const val publicPreviewList = "${PREFIX}publicPreviewList"
    const val projectRootPath = "${PREFIX}projectRootPath"
    const val generatePreviewList = "${PREFIX}generatePreviewList"
    const val generatePreviewAllList = "${PREFIX}generatePreviewAllList"
}

/**
 * @deprecated Renamed to [CompilerArg]
 */
@Suppress("ktlint:standard:property-naming", "unused")
@Deprecated("Renamed to CompilerArg", ReplaceWith("CompilerArg"))
@InternalComposePreviewLabApi
typealias KspArg = CompilerArg
