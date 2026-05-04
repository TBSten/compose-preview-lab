@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.ir

import me.tbsten.compose.preview.lab.compiler.PluginConfig
import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.fir.Keys
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

/**
 * Injects an empty body into the synthetic hint function the FIR-side
 * [me.tbsten.compose.preview.lab.compiler.fir.PreviewLabHintFirGenerator] emits.
 *
 * FIR `DeclarationGenerationExtension` cannot produce `IrBody` (bodies are an IR-only
 * concept), so the FIR generator hands the IR pass an empty hint function and relies on
 * this transformer to fill it. Without this body the JVM backend asserts on
 * `IllegalStateException: Function has no body: ...`. The marker is an empty interface,
 * so it has no constructor or members that need bodies.
 *
 * **Hint function**
 *
 * Before:
 * ```kotlin
 * public fun previewLabExport(value: PreviewLabExportMarker_<hash>): Unit  // body == null
 * ```
 *
 * After:
 * ```kotlin
 * public fun previewLabExport(value: PreviewLabExportMarker_<hash>): Unit { /* empty */ }
 * ```
 *
 * Note: the corresponding `@PreviewExportHint(fqn = ...)` annotation is **not** attached
 * here. The auto-provider FQN is fully derivable from the marker class id (the hash suffix
 * is shared with the provider name), and IR-attached annotations on FIR-generated functions
 * do not always survive to `kotlin.Metadata` on the consumer side. See
 * [PreviewListIrBuilder.collectDependencyGetters] for the marker-based derivation.
 */
internal class PreviewLabHintIrBodyFiller(
    private val pluginContext: IrPluginContext,
    private val compatContext: CompatContext,
    private val previews: List<PreviewFunctionInfo>,
    private val config: PluginConfig,
) : IrElementTransformerVoid() {

    /** Lazily-built so the dependency-getter scan only runs once per IR pass. */
    private val previewListBuilder by lazy {
        PreviewListIrBuilder(pluginContext, previews, config, compatContext)
    }

    private fun IrDeclarationOrigin.isHintFunctionOrigin(): Boolean {
        if (this !is IrDeclarationOrigin.GeneratedByPlugin) return false
        return pluginKey === Keys.PreviewLabHint
    }

    private fun IrDeclarationOrigin.isAutoProviderOrigin(): Boolean {
        if (this !is IrDeclarationOrigin.GeneratedByPlugin) return false
        return pluginKey === Keys.PreviewLabAutoProvider
    }

    /**
     * Fills bodies for the FIR-generated hint and auto-provider stubs.
     *
     * **Hint** ([Keys.PreviewLabHint]) — empty `Unit`-returning block; the function's sole
     * purpose is to occupy a KLIB IdSignature slot so downstream `referenceFunctions(...)`
     * can discover it.
     *
     * **Auto-provider** ([Keys.PreviewLabAutoProvider]) — body equivalent to:
     * ```kotlin
     * return distinctPreviewsById(thisModulePreviews + dep1.invoke() + dep2.invoke() + …)
     * ```
     * via [PreviewListIrBuilder.buildConcatenatedPreviewsExpr]. The FIR generator already
     * declared the function with the matching `previewLabAutoProvider_<hash>` name, so its
     * KLIB IdSignature is fixed at FIR-emit time — IR only fills the body, leaving the
     * symbol identity intact. That alignment is what lets a downstream consumer's
     * `pluginContext.referenceFunctions(...)` resolve to a symbol whose IdSignature matches
     * what the call IR computes, avoiding the `IrLinkageError: No function found for symbol
     * previewLabAutoProvider_<hash>` failure that the IR-only post-hoc add path was hitting.
     */
    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        if (declaration.body == null) {
            when {
                declaration.origin.isHintFunctionOrigin() -> {
                    val builder = DeclarationIrBuilder(pluginContext, declaration.symbol)
                    declaration.body = builder.irBlockBody { /* empty */ }
                }
                declaration.origin.isAutoProviderOrigin() -> {
                    val builder = DeclarationIrBuilder(pluginContext, declaration.symbol)
                    val thisModulePreviews = previewListBuilder.buildPreviewsListExpr(builder, declaration)
                    val concat = previewListBuilder.buildConcatenatedPreviewsExpr(builder, thisModulePreviews)
                    declaration.body = builder.irBlockBody { +irReturn(concat) }
                }
            }
        }
        return super.visitSimpleFunction(declaration)
    }
}
