package me.tbsten.compose.preview.lab.compiler

import me.tbsten.compose.preview.lab.compiler.compat.registerExtensionCompat
import me.tbsten.compose.preview.lab.compiler.fir.PreviewLabFirExtensionRegistrar
import me.tbsten.compose.preview.lab.compiler.ir.PreviewLabIrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

@OptIn(ExperimentalCompilerApi::class)
class ComposePreviewLabCompilerPluginRegistrar : CompilerPluginRegistrar() {
    override val pluginId: String = ComposePreviewLabCommandLineProcessor.PluginId
    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val config = PluginConfig.from(configuration)
        // Kotlin 2.3 / 2.4 で FirExtensionRegistrarAdapter / IrGenerationExtension の Companion の
        // 親クラスが変わったため、`registerExtension(...)` の解決を reflection で行う。
        registerExtensionCompat(
            FirExtensionRegistrarAdapter.Companion,
            PreviewLabFirExtensionRegistrar(config),
        )
        registerExtensionCompat(
            IrGenerationExtension.Companion,
            PreviewLabIrGenerationExtension(config),
        )
    }
}
