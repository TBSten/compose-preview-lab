package me.tbsten.compose.preview.lab.compiler.fir

import me.tbsten.compose.preview.lab.compiler.PluginConfig
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

/**
 * Compose Preview Lab の FIR extension を登録する。
 */
class PreviewLabFirExtensionRegistrar(private val config: PluginConfig) : FirExtensionRegistrar() {

    override fun ExtensionRegistrarContext.configurePlugin() {
        +::PreviewLabFirStatusTransformerExtension
    }
}
