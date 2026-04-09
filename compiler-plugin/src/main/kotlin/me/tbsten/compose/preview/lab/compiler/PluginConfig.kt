package me.tbsten.compose.preview.lab.compiler

import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

val KEY_PREVIEWS_LIST_PACKAGE = CompilerConfigurationKey<String>(
    "composePreviewLab.previewsListPackage",
)
val KEY_PUBLIC_PREVIEW_LIST = CompilerConfigurationKey<Boolean>(
    "composePreviewLab.publicPreviewList",
)
val KEY_PROJECT_ROOT_PATH = CompilerConfigurationKey<String>(
    "composePreviewLab.projectRootPath",
)
val KEY_GENERATE_PREVIEW_LIST = CompilerConfigurationKey<Boolean>(
    "composePreviewLab.generatePreviewList",
)
val KEY_GENERATE_PREVIEW_ALL_LIST = CompilerConfigurationKey<Boolean>(
    "composePreviewLab.generatePreviewAllList",
)
val KEY_DEPENDENCY_COLLECT_PREVIEWS_FQNS = CompilerConfigurationKey<String>(
    "composePreviewLab.dependencyCollectPreviewsFqns",
)

data class PluginConfig(
    val previewsListPackage: String,
    val publicPreviewList: Boolean = false,
    val projectRootPath: String? = null,
    val generatePreviewList: Boolean = true,
    val generatePreviewAllList: Boolean = true,
    val dependencyCollectPreviewsFqns: List<String> = emptyList(),
) {
    companion object {
        fun from(configuration: CompilerConfiguration): PluginConfig = PluginConfig(
            previewsListPackage = configuration.getNotNull(KEY_PREVIEWS_LIST_PACKAGE),
            publicPreviewList = configuration.get(KEY_PUBLIC_PREVIEW_LIST, false),
            projectRootPath = configuration.get(KEY_PROJECT_ROOT_PATH),
            generatePreviewList = configuration.get(KEY_GENERATE_PREVIEW_LIST, true),
            generatePreviewAllList = configuration.get(KEY_GENERATE_PREVIEW_ALL_LIST, true),
            dependencyCollectPreviewsFqns = configuration.get(KEY_DEPENDENCY_COLLECT_PREVIEWS_FQNS, "")
                .split(",")
                .filter { it.isNotBlank() },
        )
    }
}
