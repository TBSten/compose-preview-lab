package me.tbsten.compose.preview.lab.compiler

import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

val KEY_PROJECT_ROOT_PATH = CompilerConfigurationKey<String>(
    "composePreviewLab.projectRootPath",
)

data class PluginConfig(val projectRootPath: String? = null) {
    companion object {
        fun from(configuration: CompilerConfiguration): PluginConfig = PluginConfig(
            projectRootPath = configuration.get(KEY_PROJECT_ROOT_PATH),
        )
    }
}
