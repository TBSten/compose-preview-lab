package me.tbsten.compose.preview.lab.compiler

import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.compat.CompatContextSessionComponent
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.HintEntriesProvider
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.PreviewLabFirBuiltIns
import me.tbsten.compose.preview.lab.compiler.feature.transformPrivatePreviewToInternal.fir.visibilityPromotion.PreviewLabFirStatusTransformerExtension
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.fir.scopeValidation.PreviewLabFirCheckersExtension
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.fir.hintAndMarkerGeneration.PreviewHintFirGenerator
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

/**
 * Registers all of Compose Preview Lab's FIR extensions.
 *
 * Registered extensions:
 * - [PreviewLabFirBuiltIns] (`FirExtensionSessionComponent`) — session-bound
 *   [PluginConfig] wrapper, accessible from any FIR extension via
 *   `session.previewLabFirBuiltIns`. Pure-data identifiers live as top-level
 *   `val`s / `const`s inside `feature/previewCollection/` (e.g. `HINT_PACKAGE`,
 *   `COLLECTED_PREVIEW_CLASS_ID`, `PreviewHintFunctionPrefix`).
 * - [HintEntriesProvider] (`FirExtensionSessionComponent`) — session-scoped lazy
 *   cache of the per-`@Preview` hint / marker metadata list, shared by the hint
 *   and marker generators.
 * - [PreviewLabFirStatusTransformerExtension] — widens `private @Preview` functions to
 *   `internal` so generated code can call them.
 * - [PreviewHintFirGenerator] — emits the per-declaration hint
 *   (`interface PreviewHintMarker_<sanitized_fqn>_<hash>` plus one
 *   `fun previewHint_<scope>(value: PreviewHintMarker_..._<hash>?): CollectedPreview`
 *   overload per scope listed in `@ComposePreviewLabOption(collectScopes = [...])`,
 *   defaulting to `previewHint_default` when no scope is specified) for each `@Preview`.
 *   **Only registered when both** the running Kotlin compiler exposes a stable
 *   `FirDeclarationGenerationExtension.getTopLevelClassIds` /
 *   `getTopLevelCallableIds` API (Kotlin 2.3.20+, surfaced via
 *   [CompatContext.supportsFirHintGeneration]) **and** `collectPreviewsEnabled`
 *   is `true` for this module ([PluginConfig.collectPreviewsEnabled]). The IR-side
 *   cross-module discovery has separate KLIB IC-safety constraints (see
 *   [CompatContext.supportsKlibCrossModuleHint]) — those gate the IR pass, not this
 *   FIR registration, so JVM / Android consumers benefit from the per-declaration hint
 *   pipeline on Kotlin 2.3.20+ even though the KLIB IC fix only landed in 2.3.21.
 *   Skipping the registration when `collectPreviewsEnabled = false` is what guarantees
 *   that no `previewHint_<scope>(...)` overload or `PreviewHintMarker_*` interface ends
 *   up in the module's classpath.
 */
class PreviewLabFirExtensionRegistrar(private val config: PluginConfig, private val compatContext: CompatContext,) :
    FirExtensionRegistrar() {

    override fun ExtensionRegistrarContext.configurePlugin() {
        // Register the CompatContext session component first so every later component /
        // extension can resolve `session.compatContext` instead of receiving the value
        // through their constructor (= no FIR-side bucket relay). The IR side keeps
        // explicit constructor injection because it has no FIR session in scope.
        +({ session: FirSession -> CompatContextSessionComponent(session, compatContext) })
        +({ session: FirSession -> PreviewLabFirBuiltIns(session, config) })
        // Session-scoped lazy cache of `@Preview` → hint/marker metadata. Registered as
        // its own `FirExtensionSessionComponent` so both the (future) hint generator and
        // marker generator share the same instance (= the SSoT for "which @Preview
        // functions are in this session"). Holding the cache outside `PreviewLabFirBuiltIns`
        // keeps the latter scoped to plain `config` access.
        +(::HintEntriesProvider)
        +::PreviewLabFirStatusTransformerExtension
        // FIR analysis (CHECKERS) phase validation for `collectScope`. Surfaces invalid
        // values in the IDE highlighter and at compile time before the generator / IR
        // pass even start.
        //
        // **Compat gate**: `PreviewLabFirCheckersExtension`'s `simpleFunctionCheckers`
        // field is typed `Set<FirDeclarationChecker<FirNamedFunction>>`, where
        // `FirNamedFunction` is a Kotlin 2.3.20+ class (it superseded `FirSimpleFunction`
        // as the parameterization for declaration checkers). Loading the extension on
        // earlier Kotlin versions throws `NoClassDefFoundError` at plugin startup, so the
        // gate keeps the JVM classloader away from the extension class on those versions
        // (the `+::PreviewLabFirCheckersExtension` callable reference is evaluated lazily
        // — JVM class loading is triggered only when the `if` branch executes).
        // IR-pass validation (the `InvalidScopeIrError` / `NonLiteralScopeIrError` reports
        // emitted inside `ReplaceCollectPreviewsFunBody`) remains active on every Kotlin
        // version as a second-line check, so violations still surface at compile time
        // without the FIR highlighter.
        if (compatContext.supportsFirCheckers()) {
            +::PreviewLabFirCheckersExtension
        }
        if (compatContext.supportsFirHintGeneration() && config.collectPreviewsEnabled) {
            +::PreviewHintFirGenerator
        }
    }
}
