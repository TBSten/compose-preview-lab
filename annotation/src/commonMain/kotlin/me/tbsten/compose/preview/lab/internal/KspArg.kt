package me.tbsten.compose.preview.lab.internal

import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi

/**
 * **This is an internal annotation for Compose Preview Lab. Don't use this api manually.**
 */
@Suppress("ktlint:standard:property-naming")
@InternalComposePreviewLabApi
public object KspArg {
    private const val PREFIX: String = "composePreviewLab."
    public const val previewsListPackage: String = "${PREFIX}previewsListPackage"
    public const val publicPreviewList: String = "${PREFIX}publicPreviewList"
    public const val projectRootPath: String = "${PREFIX}projectRootPath"
    public const val generatePreviewList: String = "${PREFIX}generatePreviewList"
    public const val generatePreviewAllList: String = "${PREFIX}generatePreviewAllList"
}
