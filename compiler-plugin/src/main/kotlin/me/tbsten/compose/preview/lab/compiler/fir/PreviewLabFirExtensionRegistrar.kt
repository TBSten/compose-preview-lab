package me.tbsten.compose.preview.lab.compiler.fir

import me.tbsten.compose.preview.lab.compiler.PluginConfig
import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.fir.checkers.PreviewLabFirCheckersExtension
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
 *   overload per scope listed in `@ComposePreviewLabOption(collectScopes = [...])`,
 *   defaulting to `previewHint_default` when no scope is specified) for each `@Preview`.
 *   **Only registered when both** the running Kotlin compiler exposes a stable
 *   `FirDeclarationGenerationExtension.getTopLevelClassIds` /
 *   `getTopLevelCallableIds` API (Kotlin 2.3.0+, surfaced via
 *   [CompatContext.supportsFirHintGeneration]) **and** `collectPreviewsEnabled`
 *   is `true` for this module ([PluginConfig.collectPreviewsEnabled]). The IR-side
 *   cross-module discovery has separate KLIB IC-safety constraints (see
 *   [CompatContext.supportsKlibCrossModuleHint]) — those gate the IR pass, not this
 *   FIR registration, so JVM / Android consumers benefit from the per-declaration hint
 *   pipeline on Kotlin 2.3.0+ even though the KLIB IC fix only landed in 2.3.21.
 *   Skipping the registration when `collectPreviewsEnabled = false` is what guarantees
 *   that no `previewHint_<scope>(...)` overload or `PreviewHintMarker_*` interface ends
 *   up in the module's classpath.
 */
class PreviewLabFirExtensionRegistrar(private val config: PluginConfig) : FirExtensionRegistrar() {

    override fun ExtensionRegistrarContext.configurePlugin() {
        +({ session: FirSession -> PreviewLabFirBuiltIns(session, config) })
        +::PreviewLabFirStatusTransformerExtension
        // Always-on validation. Runs in the FIR analysis (CHECKERS) phase so invalid
        // collectScope values surface in the IDE highlighter and at compile time before
        // the generator / IR pass even start. The checkers extension is the canonical
        // single-source-of-truth validation point — it works regardless of build system,
        // platform, or Kotlin version (no compat gate needed because the checkers API has
        // been stable since 2.0).
        +::PreviewLabFirCheckersExtension
        val compat = CompatContext.load()
        if (compat.supportsFirHintGeneration() && config.collectPreviewsEnabled) {
            +({ session: FirSession -> PreviewHintFirGenerator(session, compat) })
        }
    }
}
