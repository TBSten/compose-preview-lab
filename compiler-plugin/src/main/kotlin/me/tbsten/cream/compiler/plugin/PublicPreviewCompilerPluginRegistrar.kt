package me.tbsten.cream.compiler.plugin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@Suppress("unused")
@OptIn(ExperimentalCompilerApi::class)
class PublicPreviewCompilerPluginRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        println("PublicPreviewCompilerPluginRegistrar.registerExtensions")
        if (configuration[PublicPreviewCommandLineProcessor.KEY_ENABLED] == false) {
            return
        }

        val annotations = configuration[PublicPreviewCommandLineProcessor.KEY_ANNOTATIONS]
            ?: error("PublicPreviewPlugin is enabled but no annotations are specified.")
        IrGenerationExtension.registerExtension(PublicPreviewIrGenerationExtensionExtension(annotations))
    }
}
