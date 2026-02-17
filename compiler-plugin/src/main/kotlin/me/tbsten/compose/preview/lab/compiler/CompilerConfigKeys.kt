package me.tbsten.compose.preview.lab.compiler

import org.jetbrains.kotlin.config.CompilerConfigurationKey

internal object CompilerConfigKeys {
    val PREVIEWS_LIST_PACKAGE = CompilerConfigurationKey<String>("previewsListPackage")
    val PUBLIC_PREVIEW_LIST = CompilerConfigurationKey<Boolean>("publicPreviewList")
    val PROJECT_ROOT_PATH = CompilerConfigurationKey<String>("projectRootPath")
    val GENERATE_PREVIEW_LIST = CompilerConfigurationKey<Boolean>("generatePreviewList")
    val GENERATE_PREVIEW_ALL_LIST = CompilerConfigurationKey<Boolean>("generatePreviewAllList")
}
