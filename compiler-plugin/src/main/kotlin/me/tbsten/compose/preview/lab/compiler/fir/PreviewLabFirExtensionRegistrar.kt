package me.tbsten.compose.preview.lab.compiler.fir

import me.tbsten.compose.preview.lab.compiler.PluginConfig
import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

/**
 * Registers all of Compose Preview Lab's FIR extensions.
 *
 * Registered extensions:
 * - [PreviewLabFirBuiltIns] (`FirExtensionSessionComponent`) — shared FQN/CallableId constants
 *   plus [PluginConfig], accessible from any FIR extension via `session.previewLabFirBuiltIns`.
 * - [PreviewLabFirStatusTransformerExtension] — widens `private @Preview` functions to
 *   `internal` so generated code can call them.
 * - [PreviewHintFirGenerator] — emits the per-declaration hint
 *   (`interface PreviewHintMarker_<sanitized_fqn>_<hash>` plus one
 *   `fun previewHint_<scope>(value: PreviewHintMarker_..._<hash>?): CollectedPreview`
 *   overload per scope listed in `@ComposePreviewLabOption(collectScope = [...])`,
 *   defaulting to `previewHint_default` when no scope is specified) for each `@Preview`.
 *   **Only registered when both** the running Kotlin compiler supports it (Kotlin 2.3.21+,
 *   surfaced via [CompatContext.supportsKlibCrossModuleHint]) **and** `collectPreviewsEnabled`
 *   is `true` for this module ([PluginConfig.collectPreviewsEnabled]). Skipping the
 *   registration when `collectPreviewsEnabled = false` is what guarantees that no
 *   `previewHint_<scope>(...)` overload or `PreviewHintMarker_*` interface ends up in the
 *   module's classpath.
 */
class PreviewLabFirExtensionRegistrar(
    private val config: PluginConfig,
    private val messageCollector: MessageCollector = MessageCollector.NONE,
) : FirExtensionRegistrar() {

    override fun ExtensionRegistrarContext.configurePlugin() {
        +({ session: FirSession -> PreviewLabFirBuiltIns(session, config) })
        +::PreviewLabFirStatusTransformerExtension
        val compat = CompatContext.load()
        if (compat.supportsKlibCrossModuleHint() && config.collectPreviewsEnabled) {
            +({ session: FirSession -> PreviewHintFirGenerator(session, compat, messageCollector) })
        }
    }
}
