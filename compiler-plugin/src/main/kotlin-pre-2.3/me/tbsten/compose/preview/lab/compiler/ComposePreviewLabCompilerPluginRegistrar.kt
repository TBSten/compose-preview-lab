package me.tbsten.compose.preview.lab.compiler

import me.tbsten.compose.preview.lab.compiler.fir.PreviewLabFirExtensionRegistrar
import me.tbsten.compose.preview.lab.compiler.ir.PreviewLabIrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

@OptIn(ExperimentalCompilerApi::class)
class ComposePreviewLabCompilerPluginRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val config = PluginConfig.from(configuration)
        FirExtensionRegistrarAdapter.registerExtension(PreviewLabFirExtensionRegistrar(config))
        IrGenerationExtension.registerExtension(PreviewLabIrGenerationExtension(config))
    }
}
