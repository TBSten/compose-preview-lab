package me.tbsten.compose.preview.lab.compiler

import me.tbsten.compose.preview.lab.compiler.compat.registerExtensionCompat
import me.tbsten.compose.preview.lab.compiler.fir.PreviewLabFirExtensionRegistrar
import me.tbsten.compose.preview.lab.compiler.ir.PreviewLabIrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

@OptIn(ExperimentalCompilerApi::class)
class ComposePreviewLabCompilerPluginRegistrar : CompilerPluginRegistrar() {
    override val pluginId: String = ComposePreviewLabCommandLineProcessor.PluginId
    override val supportsK2: Boolean = true

    /**
     * Registers the FIR and IR extensions for Compose Preview Lab.
     *
     * Extensions registered:
     * - [PreviewLabFirExtensionRegistrar] (via [FirExtensionRegistrarAdapter]) — registers FIR
     *   status transformer that widens `private @Preview` functions to `internal`.
     * - [PreviewLabIrGenerationExtension] (via [IrGenerationExtension]) — collects `@Preview`
     *   functions and rewrites `collectModulePreviews()` / `collectAllModulePreviews()` call sites.
     *
     * `registerExtensionCompat` is used instead of the standard companion `registerExtension`
     * because the parent class of `FirExtensionRegistrarAdapter.Companion` and
     * `IrGenerationExtension.Companion` changed between Kotlin 2.3 and 2.4, producing
     * different JVM call-site signatures. The reflective wrapper abstracts that away.
     */
    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val config = PluginConfig.from(configuration)
        val messageCollector = configuration.get(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY)
            ?: MessageCollector.NONE
        // The parent class of the FirExtensionRegistrarAdapter / IrGenerationExtension companions
        // changed between Kotlin 2.3 and 2.4, so resolve `registerExtension(...)` reflectively.
        registerExtensionCompat(
            FirExtensionRegistrarAdapter.Companion,
            PreviewLabFirExtensionRegistrar(config),
        )
        registerExtensionCompat(
            IrGenerationExtension.Companion,
            PreviewLabIrGenerationExtension(config, messageCollector),
        )
    }
}
