package me.tbsten.cream.compiler.plugin

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

@OptIn(ExperimentalCompilerApi::class)
class PublicPreviewCommandLineProcessor : CommandLineProcessor {
    companion object {
        val KEY_ENABLED = CompilerConfigurationKey.create<Boolean>("my-plugin-enabled")
        val KEY_ANNOTATIONS = CompilerConfigurationKey.create<List<String>>("my-plugin-annotations")
    }

    init {
        println("PublicPreviewCommandLineProcessor.init()")
    }

    override val pluginId: String = "cream-public-preview-plugin"

    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        CliOption(
            optionName = "enabled",
            valueDescription = "true|false",
            description = "Whether MyPlugin is enabled or not.",
        ),
        CliOption(
            optionName = "publicPreviewAnnotation",
            valueDescription = "annotation",
            description = "Annotation to be processed by MyPlugin.",
            allowMultipleOccurrences = true,
        ),
    )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) =
        when (option.optionName) {
            "enabled" -> configuration.put(KEY_ENABLED, value.toBoolean())
            "publicPreviewAnnotation" -> configuration.appendList(KEY_ANNOTATIONS, value)
            else -> error("Unexpected config option ${option.optionName}")
        }.also {
            println("- processOption: ${option.optionName}: $value")
        }
}
