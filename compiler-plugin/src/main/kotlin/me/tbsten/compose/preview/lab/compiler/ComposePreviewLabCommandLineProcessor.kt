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
        CliOption(
            optionName = OptionDefaultCollectScope,
            valueDescription = "<scope>",
            description = "Module-level default scope substituted whenever a per-@Preview / " +
                "per-call scope is the literal \"default\". Must match [A-Za-z0-9_]+ " +
                "(default: \"default\")",
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
            OptionDefaultCollectScope -> {
                // The value is embedded into the synthetic `previewHint_<scope>` Kotlin
                // identifier, so reject anything that wouldn't parse as one. Caught here
                // (option-processing time = before any source file is compiled) the user
                // sees the error before opening their first build log line — far earlier
                // than catching the same typo at FIR / IR phase.
                if (!ScopeIdentifierRegex.matches(value)) {
                    throw CliOptionProcessingException(
                        "Invalid value for $OptionDefaultCollectScope: \"$value\". " +
                            "Expected a Kotlin-identifier-safe scope matching [A-Za-z0-9_]+ " +
                            "(it is embedded into the synthetic previewHint_<scope> function name).",
                    )
                }
                configuration.put(KEY_DEFAULT_COLLECT_SCOPE, value)
            }
            else -> throw CliOptionProcessingException("Unknown option: ${option.optionName}")
        }
    }

    companion object {
        const val PluginId = "me.tbsten.compose.preview.lab.compiler"
        const val OptionProjectRootPath = "projectRootPath"
        const val OptionCollectPreviewsEnabled = "collectPreviewsEnabled"
        const val OptionDefaultCollectScope = "defaultCollectScope"

        /**
         * Mirror of [PreviewLabConstants.SCOPE_VALIDATION_REGEX] — kept literal here to
         * avoid pulling the [PreviewLabConstants] object into the CLI processor entry point
         * (this file runs before any FIR / IR plugin component is loaded).
         */
        private val ScopeIdentifierRegex: Regex = Regex("[A-Za-z0-9_]+")
    }
}
