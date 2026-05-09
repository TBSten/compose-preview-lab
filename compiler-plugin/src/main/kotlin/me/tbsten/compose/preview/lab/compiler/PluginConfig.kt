package me.tbsten.compose.preview.lab.compiler

import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

internal val KEY_PROJECT_ROOT_PATH = CompilerConfigurationKey<String>(
    "composePreviewLab.projectRootPath",
)

internal val KEY_COLLECT_PREVIEWS_ENABLED = CompilerConfigurationKey<Boolean>(
    "composePreviewLab.collectPreviewsEnabled",
)

internal val KEY_DEFAULT_COLLECT_SCOPE = CompilerConfigurationKey<String>(
    "composePreviewLab.defaultCollectScope",
)

data class PluginConfig(
    val projectRootPath: String? = null,
    /**
     * Module-level on/off for the per-declaration hint pipeline.
     *
     * When `false`, `PreviewHintFirGenerator` is not registered for this module (no marker
     * interfaces and no `previewHint(...)` overloads are emitted) and any
     * `collectModulePreviews()` / `collectAllModulePreviews()` call site in this module is
     * reported as a `CompilerMessageSeverity.ERROR` by [me.tbsten.compose.preview.lab.compiler.ir.PreviewLabIrBodyFiller].
     * Defaults to `true` so existing builds keep working unchanged.
     */
    val collectPreviewsEnabled: Boolean = true,
    /**
     * Module-level default scope substituted whenever a per-`@Preview` `collectScope` value
     * or a `collect[All]ModulePreviews(scope = ...)` argument equals
     * `ComposePreviewLabOption.DefaultCollectScope` (i.e. the literal `"default"`).
     *
     * Setting this via `composePreviewLab.collectPreviews.defaultCollectScope = "acme_ui"`
     * pins every unscoped `@Preview` in the module to `previewHint_acme_ui`, so the module's
     * previews stop landing in a downstream consumer's default-scope `collectAllModulePreviews()`
     * call. Defaults to `"default"` so existing builds keep emitting `previewHint_default`.
     *
     * The string is validated against `[A-Za-z0-9_]+` at command-line-processor time so an
     * invalid Gradle DSL value fails the build before any source is compiled.
     */
    val defaultCollectScope: String = ComposePreviewLabOption.DefaultCollectScope,
) {
    companion object {
        fun from(configuration: CompilerConfiguration): PluginConfig = PluginConfig(
            projectRootPath = configuration.get(KEY_PROJECT_ROOT_PATH),
            collectPreviewsEnabled = configuration.get(KEY_COLLECT_PREVIEWS_ENABLED) ?: true,
            defaultCollectScope = configuration.get(KEY_DEFAULT_COLLECT_SCOPE)
                ?: ComposePreviewLabOption.DefaultCollectScope,
        )
    }
}
