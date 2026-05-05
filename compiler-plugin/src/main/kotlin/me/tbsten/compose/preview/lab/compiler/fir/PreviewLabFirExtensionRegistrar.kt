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
 * - [PreviewHintFirGenerator] — `@Preview` 1 個ごとに per-declaration hint
 *   (`interface PreviewHintMarker_<hash>` + `fun previewHint(value: PreviewHintMarker_<hash>?): CollectedPreview`)
 *   を emit する Metro 風 generator。
 *   **Only registered when the running Kotlin compiler supports it** (Kotlin 2.3.21+,
 *   surfaced via [CompatContext.supportsKlibCrossModuleHint])。 古い Kotlin では
 *   `collectAllModulePreviews()` 自体が動かないため、 T06 の FIR Checker が call site で
 *   compile-time error を報告する。
 */
class PreviewLabFirExtensionRegistrar(private val config: PluginConfig) : FirExtensionRegistrar() {

    override fun ExtensionRegistrarContext.configurePlugin() {
        +({ session: FirSession -> PreviewLabFirBuiltIns(session, config) })
        +::PreviewLabFirStatusTransformerExtension
        if (CompatContext.load().supportsKlibCrossModuleHint()) {
            +::PreviewHintFirGenerator
        }
    }
}
