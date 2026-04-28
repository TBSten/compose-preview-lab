package me.tbsten.compose.preview.lab.compiler

import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

val KEY_PROJECT_ROOT_PATH = CompilerConfigurationKey<String>(
    "composePreviewLab.projectRootPath",
)
val KEY_DEPENDENCY_COLLECT_PREVIEWS_FQNS = CompilerConfigurationKey<String>(
    "composePreviewLab.dependencyCollectPreviewsFqns",
)

data class PluginConfig(val projectRootPath: String? = null, val dependencyCollectPreviewsFqns: List<String> = emptyList(),) {
    companion object {
        fun from(configuration: CompilerConfiguration): PluginConfig = PluginConfig(
            projectRootPath = configuration.get(KEY_PROJECT_ROOT_PATH),
            dependencyCollectPreviewsFqns = configuration.get(KEY_DEPENDENCY_COLLECT_PREVIEWS_FQNS, "")
                .split(",")
                .filter { it.isNotBlank() },
        )
    }
}
