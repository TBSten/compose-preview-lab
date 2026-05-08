package me.tbsten.compose.preview.lab.compiler

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CliOptionProcessingException
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
        CliOption(
            optionName = OptionCollectPreviewsEnabled,
            valueDescription = "<true|false>",
            description = "Whether this module emits per-declaration preview hints and lets " +
                "collect[All]ModulePreviews() compile (default: true)",
            required = false,
        ),
    )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration,) {
        when (option.optionName) {
            OptionProjectRootPath -> configuration.put(KEY_PROJECT_ROOT_PATH, value)
            OptionCollectPreviewsEnabled -> {
                // Strict parse: a typo like `ture` would otherwise silently flip the module
                // into the disabled state, where every collect[All]ModulePreviews() call site
                // becomes a compile-time error. Failing fast turns the typo into an
                // option-processing diagnostic the user can act on.
                val parsed = value.toBooleanStrictOrNull()
                    ?: throw CliOptionProcessingException(
                        "Invalid value for $OptionCollectPreviewsEnabled: \"$value\". " +
                            "Expected exactly \"true\" or \"false\".",
                    )
                configuration.put(KEY_COLLECT_PREVIEWS_ENABLED, parsed)
            }
            else -> throw CliOptionProcessingException("Unknown option: ${option.optionName}")
        }
    }

    companion object {
        const val PluginId = "me.tbsten.compose.preview.lab.compiler"
        const val OptionProjectRootPath = "projectRootPath"
        const val OptionCollectPreviewsEnabled = "collectPreviewsEnabled"
    }
}
