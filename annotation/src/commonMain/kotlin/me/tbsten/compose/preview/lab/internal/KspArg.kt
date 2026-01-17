package me.tbsten.compose.preview.lab.internal

import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi

/**
 * **This is an internal annotation for Compose Preview Lab. Don't use this api manually.**
 */
@Suppress("ktlint:standard:property-naming")
@InternalComposePreviewLabApi
object KspArg {
    private const val PREFIX = "composePreviewLab."
    const val previewsListPackage = "${PREFIX}previewsListPackage"
    const val publicPreviewList = "${PREFIX}publicPreviewList"
    const val projectRootPath = "${PREFIX}projectRootPath"
    const val generatePreviewList = "${PREFIX}generatePreviewList"
    const val generatePreviewAllList = "${PREFIX}generatePreviewAllList"
    const val generateAutoField = "${PREFIX}generateAutoField"
}
