package me.tbsten.compose.preview.lab.compiler

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
class ComposePreviewLabCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String = PluginId

    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        CliOption(
            optionName = OptionProjectRootPath,
            valueDescription = "<path>",
            description = "Root path of the project",
            required = false,
        ),
    )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration,) {
        when (option.optionName) {
            OptionProjectRootPath -> configuration.put(KEY_PROJECT_ROOT_PATH, value)
            else -> error("Unknown option: ${option.optionName}")
        }
    }

    companion object {
        const val PluginId = "me.tbsten.compose.preview.lab.compiler"
        const val OptionProjectRootPath = "projectRootPath"
    }
}
