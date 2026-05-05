package me.tbsten.compose.preview.lab.compiler.fir

import me.tbsten.compose.preview.lab.compiler.PluginConfig
import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

/**
 * Compose Preview Lab の FIR extension を登録する。
 *
 * Registered extensions:
 * - [PreviewLabFirBuiltIns] (`FirExtensionSessionComponent`) — shared FQN/CallableId constants
 *   plus [PluginConfig], accessible from any FIR extension via `session.previewLabFirBuiltIns`.
 * - [PreviewLabFirStatusTransformerExtension] — widens `private @Preview` functions to
 *   `internal` so generated code can call them.
 * - [PreviewLabHintFirGenerator] — generates per-export marker classes and hint functions for
 *   KLIB-safe cross-module aggregation. **Only registered when the running Kotlin compiler
 *   supports it** (Kotlin 2.3.21+, surfaced via [CompatContext.supportsKlibCrossModuleHint]),
 *   so older 2.x lines keep using the existing IR-based hint path on JVM.
 */
class PreviewLabFirExtensionRegistrar(private val config: PluginConfig) : FirExtensionRegistrar() {

    override fun ExtensionRegistrarContext.configurePlugin() {
        +({ session: FirSession -> PreviewLabFirBuiltIns(session, config) })
        +::PreviewLabFirStatusTransformerExtension
        if (CompatContext.load().supportsKlibCrossModuleHint()) {
            +::PreviewLabHintFirGenerator
            +::PreviewHintFirGeneratorV2
        }
    }
}
