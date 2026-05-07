@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.ir

import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.compat.IrDeclarationOriginCompat
import me.tbsten.compose.preview.lab.compiler.fir.PreviewLabFirBuiltIns
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classFqName

/**
 * Discovers per-declaration hints **emitted by dependency modules**. Performs a fixed-name
 * lookup of `me.tbsten.compose.preview.lab.hints.previewHint` via
 * `pluginContext.referenceFunctions` so every hint on the classpath is found.
 *
 * **Sample call**:
 * ```kotlin
 * val hints = discoverHints(pluginContext, compatContext)
 * // → returns the IrSimpleFunctions emitted by dependency modules:
 * //   `previewHint(value: PreviewHintMarker_uilib_button_MyButton_<hash1>?): CollectedPreview`,
 * //   `previewHint(value: PreviewHintMarker_uilib_text_MyText_<hash2>?): CollectedPreview`, ...
 * //   (hints from the current module are filtered out)
 * ```
 *
 * # Design points
 *
 * ## Fixed name + marker param
 *
 * The hint function name is fixed (`previewHint`); the marker class on the parameter is
 * unique per `@Preview`. The KLIB IdSignature is derived from `(name, paramTypes)`, so a
 * different marker yields a different IdSignature. A single
 * `referenceFunctions(fixed-name)` call discovers every hint across the classpath.
 *
 * ```kotlin
 * pluginContext.referenceFunctions(CallableId(HINT_PACKAGE, Name.identifier("previewHint")))
 * // → returns every previewHint(...) from this module + dependency modules
 * ```
 *
 * ## Cross-module gate
 *
 * The caller
 * ([PreviewLabIrBodyFiller.replaceCollectPreviewsProperty][me.tbsten.compose.preview.lab.compiler.ir.PreviewLabIrBodyFiller])
 * pre-checks [CompatContext.supportsKlibCrossModuleHint] and aborts the IR transform with
 * an early error report when not supported, so by the time [discoverHints] is invoked the
 * gate condition is assumed to hold.
 *
 * ## Filter conditions
 *
 * - [IrDeclarationOriginCompat.IR_EXTERNAL_DECLARATION_STUB] origin (= comes from another
 *   module)
 * - exactly one marker parameter whose class lives in the hint package
 * - the marker class short name starts with `PreviewHintMarker_`
 * - the return type is `CollectedPreview`
 *
 * # References
 *
 * `referenceFunctions(callableId)` carries a K2 deprecation warning, but the recommended
 * replacements (`finderForBuiltins` / `finderForSource(fromFile)`) are scoped to
 * **builtins / a single file** and do not cover walking the whole classpath by fixed
 * name. We continue to use the existing API for now and will migrate when K2 grows a
 * classpath-wide finder (follow-up).
 */
internal fun discoverHints(pluginContext: IrPluginContext, compatContext: CompatContext,): List<IrSimpleFunction> {
    if (!compatContext.supportsKlibCrossModuleHint()) return emptyList()

    // `referenceFunctions` is deprecated in K2, but the recommended replacements
    // (`finderForBuiltins` / `finderForSource`) do not support classpath-wide fixed-name
    // lookup, so we keep using the existing API. See KDoc for details.
    @Suppress("DEPRECATION")
    val hintSymbols = pluginContext.referenceFunctions(PreviewLabFirBuiltIns.HINT_FUNCTION_CALLABLE_ID)

    return hintSymbols.mapNotNull { hintSymbol ->
        val hintFunction = hintSymbol.owner

        // Hints emitted by the current module are already listed via thisModulePreviews,
        // so drop them here.
        if (hintFunction.origin != IrDeclarationOriginCompat.IR_EXTERNAL_DECLARATION_STUB) return@mapNotNull null

        // Sanity-check: exactly one marker parameter and a CollectedPreview return type.
        val regularParams = hintFunction.parameters.filter { it.kind == IrParameterKind.Regular }
        if (regularParams.size != 1) return@mapNotNull null
        val markerFqn = regularParams[0].type.classFqName ?: return@mapNotNull null
        if (markerFqn.parent() != PreviewLabFirBuiltIns.HINT_PACKAGE) return@mapNotNull null
        if (!markerFqn.shortName().asString().startsWith(PreviewLabFirBuiltIns.PreviewHintMarkerPrefix)) return@mapNotNull null
        if (hintFunction.returnType.classFqName?.asString() != CollectedPreviewFqn) return@mapNotNull null

        hintFunction
    }
}

private const val CollectedPreviewFqn = "me.tbsten.compose.preview.lab.CollectedPreview"
