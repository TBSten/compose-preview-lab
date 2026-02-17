package me.tbsten.compose.preview.lab.compiler

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

class ComposePreviewLabCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String = PLUGIN_ID

    override val pluginOptions: Collection<CliOption> = listOf(
        CliOption(
            optionName = OPTION_PREVIEWS_LIST_PACKAGE,
            valueDescription = "<package>",
            description = "Package name for generated PreviewList",
            required = false,
        ),
        CliOption(
            optionName = OPTION_PUBLIC_PREVIEW_LIST,
            valueDescription = "<true|false>",
            description = "Whether to make PreviewList public",
            required = false,
        ),
        CliOption(
            optionName = OPTION_PROJECT_ROOT_PATH,
            valueDescription = "<path>",
            description = "Project root path for resolving relative file paths",
            required = false,
        ),
        CliOption(
            optionName = OPTION_GENERATE_PREVIEW_LIST,
            valueDescription = "<true|false>",
            description = "Whether to generate PreviewList",
            required = false,
        ),
        CliOption(
            optionName = OPTION_GENERATE_PREVIEW_ALL_LIST,
            valueDescription = "<true|false>",
            description = "Whether to generate PreviewAllList",
            required = false,
        ),
    )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        when (option.optionName) {
            OPTION_PREVIEWS_LIST_PACKAGE -> configuration.put(CompilerConfigKeys.PREVIEWS_LIST_PACKAGE, value)
            OPTION_PUBLIC_PREVIEW_LIST -> configuration.put(CompilerConfigKeys.PUBLIC_PREVIEW_LIST, value.toBooleanStrictOrNull() ?: false)
            OPTION_PROJECT_ROOT_PATH -> configuration.put(CompilerConfigKeys.PROJECT_ROOT_PATH, value)
            OPTION_GENERATE_PREVIEW_LIST -> configuration.put(CompilerConfigKeys.GENERATE_PREVIEW_LIST, value.toBooleanStrictOrNull() ?: true)
            OPTION_GENERATE_PREVIEW_ALL_LIST -> configuration.put(CompilerConfigKeys.GENERATE_PREVIEW_ALL_LIST, value.toBooleanStrictOrNull() ?: true)
        }
    }

    companion object {
        const val PLUGIN_ID = "me.tbsten.compose.preview.lab.compiler"
        const val OPTION_PREVIEWS_LIST_PACKAGE = "previewsListPackage"
        const val OPTION_PUBLIC_PREVIEW_LIST = "publicPreviewList"
        const val OPTION_PROJECT_ROOT_PATH = "projectRootPath"
        const val OPTION_GENERATE_PREVIEW_LIST = "generatePreviewList"
        const val OPTION_GENERATE_PREVIEW_ALL_LIST = "generatePreviewAllList"
    }
}
