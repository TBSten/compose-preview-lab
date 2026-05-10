@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.ir

import me.tbsten.compose.preview.lab.compiler.PreviewLabConstants
import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.compat.IrDeclarationOriginCompat
import me.tbsten.compose.preview.lab.compiler.utils.ir.requiresKlibIcSafetyForCrossModuleHint
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.name.FqName

/** FQN of the marker annotation that the FIR generator stamps on every plugin-emitted hint. */
private val SyntheticPreviewHintFqn = FqName("me.tbsten.compose.preview.lab.SyntheticPreviewHint")

/**
 * Discovers per-declaration hints **emitted by dependency modules** for a given
 * collection scope. Performs a per-scope `previewHint_<scope>` lookup via
 * `pluginContext.referenceFunctions` so every hint on the classpath that participates in
 * [scope] is found. Hints emitted under a different scope are skipped without inspection.
 *
 * **Sample call**:
 * ```kotlin
 * val hints = discoverHints(pluginContext, compatContext, scope = "design")
 * // → returns the IrSimpleFunctions emitted by dependency modules under "design":
 * //   `previewHint_design(value: PreviewHintMarker_uilib_design_MyButton_<hash1>?): CollectedPreview`,
 * //   `previewHint_design(value: PreviewHintMarker_uilib_design_MyText_<hash2>?): CollectedPreview`, ...
 * //   (hints from the current module and hints from other scopes are filtered out)
 * ```
 *
 * # Design points
 *
 * ## Scope-suffixed name + marker param
 *
 * The hint function name encodes the scope (`previewHint_<scope>`); the marker class on the
 * parameter is unique per `@Preview`. The KLIB IdSignature is derived from
 * `(name, paramTypes)`, so a different marker yields a different IdSignature. A single
 * `referenceFunctions(per-scope name)` call discovers every hint across the classpath that
 * participates in the requested scope, with no after-the-fact filtering needed.
 *
 * ```kotlin
 * pluginContext.referenceFunctions(CallableId(HINT_PACKAGE, Name.identifier("previewHint_design")))
 * // → returns every previewHint_design(...) from this module + dependency modules
 * ```
 *
 * ## Cross-module gate
 *
 * The caller
 * ([PreviewLabIrBodyFiller.replaceCollectPreviewsProperty][me.tbsten.compose.preview.lab.compiler.ir.PreviewLabIrBodyFiller])
 * pre-checks the platform-aware gate (JVM / Android: only
 * `CompatContext.supportsFirHintGeneration` required = 2.3.20+; KLIB targets:
 * additionally `CompatContext.supportsKlibCrossModuleHint` required = 2.3.21+) and
 * aborts the IR transform with an early error report when KLIB IC safety is
 * unavailable. By the time [discoverHints] is invoked the gate condition is assumed to
 * hold; the fall-through `return emptyList()` here is a defensive belt-and-braces so a
 * misconfigured caller (= calling this directly without the upstream check) returns
 * nothing rather than triggering a stale-IC walk.
 *
 * ## Filter conditions
 *
 * - [IrDeclarationOriginCompat.IR_EXTERNAL_DECLARATION_STUB] origin (= comes from another
 *   module)
 * - exactly one marker parameter whose class lives in the hint package
 * - the marker class short name starts with `PreviewHintMarker_`
 * - the return type is `CollectedPreview`
 * - the hint function is annotated with
 *   `@me.tbsten.compose.preview.lab.SyntheticPreviewHint` (the FIR generator stamps this
 *   on every hint it emits). A function that lives in the hint package but lacks this
 *   marker is rejected with a compile-time warning so a third-party library cannot squat
 *   the namespace and inject previews into the consumer's `collectAllModulePreviews`
 *   result. See `PreviewHintFirGenerator.markAsInternalSyntheticHint`.
 *
 * ## Cross-artifact duplicate detection (best effort)
 *
 * After filtering, if two or more dependency modules contribute a hint with the **same
 * marker FQN** (= same source `@Preview` signature, same hash) the function emits a
 * compile-time warning. In practice the warning is hard to trigger because both the JVM
 * classloader and the KLIB linker collapse same-FQN duplicates down to a single symbol,
 * so `referenceFunctions` only ever returns one entry. The detection logic stays for
 * unusual setups (e.g. partial incremental-compile states where the symbol provider
 * surfaces both declarations) and to make the intent explicit; the runtime
 * `distinctPreviewsById` is the user-visible signal in the common case, and the
 * `distinctPreviewsById` KDoc documents the silent edge case honestly.
 *
 * # References
 *
 * `referenceFunctions(callableId)` carries a K2 deprecation warning, but the recommended
 * replacements (`finderForBuiltins` / `finderForSource(fromFile)`) are scoped to
 * **builtins / a single file** and do not cover walking the whole classpath by fixed
 * name. We continue to use the existing API for now and will migrate when K2 grows a
 * classpath-wide finder (follow-up).
 */
internal fun discoverHints(
    pluginContext: IrPluginContext,
    compatContext: CompatContext,
    scope: String,
): List<IrSimpleFunction> {
    if (!compatContext.supportsFirHintGeneration()) return emptyList()
    if (pluginContext.platform.requiresKlibIcSafetyForCrossModuleHint &&
        !compatContext.supportsKlibCrossModuleHint()
    ) {
        return emptyList()
    }

    // `referenceFunctions` is deprecated in K2, but the recommended replacements
    // (`finderForBuiltins` / `finderForSource`) do not support classpath-wide fixed-name
    // lookup, so we keep using the existing API. See KDoc for details.
    @Suppress("DEPRECATION")
    val hintSymbols = pluginContext.referenceFunctions(PreviewLabConstants.hintFunctionCallableId(scope))

    // `messageCollector` is itself deprecated in favour of `diagnosticReporter`, but
    // emitting sourceless diagnostics through `IrDiagnosticReporter` requires a
    // `KtSourcelessDiagnosticFactory` instance — the message collector path stays
    // shorter for the two warnings we emit here. Migrate together with the
    // `referenceFunctions` migration.
    @Suppress("DEPRECATION")
    val messageCollector = pluginContext.messageCollector

    val structuralCandidates = hintSymbols.mapNotNull { hintSymbol ->
        val hintFunction = hintSymbol.owner

        // Hints emitted by the current module are already listed via thisModulePreviews,
        // so drop them here.
        if (hintFunction.origin != IrDeclarationOriginCompat.IR_EXTERNAL_DECLARATION_STUB) return@mapNotNull null

        // Sanity-check: exactly one marker parameter and a CollectedPreview return type.
        val regularParams = hintFunction.parameters.filter { it.kind == IrParameterKind.Regular }
        if (regularParams.size != 1) return@mapNotNull null
        val markerFqn = regularParams[0].type.classFqName ?: return@mapNotNull null
        if (markerFqn.parent() != PreviewLabConstants.HINT_PACKAGE) return@mapNotNull null
        if (!markerFqn.shortName().asString().startsWith(PreviewLabConstants.PreviewHintMarkerPrefix)) return@mapNotNull null
        if (hintFunction.returnType.classFqName?.asString() != CollectedPreviewFqn) return@mapNotNull null

        hintFunction to markerFqn
    }

    // Squatting guard: structural shape alone is not enough — anyone could declare a
    // function in `me.tbsten.compose.preview.lab.hints` with the right shape and have it
    // included in a downstream `collectAllModulePreviews()`. Require the
    // plugin-stamped `@SyntheticPreviewHint` marker as positive proof of authenticity.
    val authentic = structuralCandidates.filter { (hintFunction, markerFqn) ->
        if (hintFunction.hasAnnotation(SyntheticPreviewHintFqn)) {
            true
        } else {
            messageCollector.report(
                CompilerMessageSeverity.WARNING,
                "Compose Preview Lab: a function in '${PreviewLabConstants.HINT_PACKAGE.asString()}' " +
                    "matching the per-scope hint shape is missing the @SyntheticPreviewHint marker " +
                    "(marker parameter '$markerFqn'). Only the Compose Preview Lab compiler plugin " +
                    "should emit declarations into this package; the candidate will be ignored to " +
                    "prevent namespace squatting.",
            )
            false
        }
    }

    // Cross-artifact dup detection: hints whose origin is IR_EXTERNAL_DECLARATION_STUB
    // and that share a marker FQN come from two different external modules (the FIR
    // generator de-duplicates within one module by `markerShortName`). Same marker FQN
    // ⇒ same source `@Preview` signature ⇒ the classpath is pulling the same preview
    // from multiple artifacts. Runtime `distinctPreviewsById` collapses the duplicates,
    // but a compile-time signal makes the over-pull visible.
    authentic.groupBy { (_, markerFqn) -> markerFqn }
        .filterValues { it.size > 1 }
        .forEach { (markerFqn, duplicates) ->
            messageCollector.report(
                CompilerMessageSeverity.WARNING,
                "Compose Preview Lab: ${duplicates.size} synthetic hint functions on the classpath " +
                    "share marker '$markerFqn' (scope = '$scope'). The same `@Preview` is included via " +
                    "multiple artifacts; runtime `distinctPreviewsById` retains the first occurrence " +
                    "only. Verify the dependency tree if you did not intend to publish the same " +
                    "preview source from two coordinates.",
            )
        }

    return authentic.map { (hintFunction, _) -> hintFunction }
}

private const val CollectedPreviewFqn = "me.tbsten.compose.preview.lab.CollectedPreview"
