package me.tbsten.compose.preview.lab.compiler

import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

val KEY_PROJECT_ROOT_PATH = CompilerConfigurationKey<String>(
    "composePreviewLab.projectRootPath",
)

val KEY_COLLECT_PREVIEWS_ENABLED = CompilerConfigurationKey<Boolean>(
    "composePreviewLab.collectPreviewsEnabled",
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
) {
    companion object {
        fun from(configuration: CompilerConfiguration): PluginConfig = PluginConfig(
            projectRootPath = configuration.get(KEY_PROJECT_ROOT_PATH),
            collectPreviewsEnabled = configuration.get(KEY_COLLECT_PREVIEWS_ENABLED) ?: true,
        )
    }
}
