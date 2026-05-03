@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.ir

import me.tbsten.compose.preview.lab.compiler.fir.Keys
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlockBody
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
 * public fun previewLabExport(value: PreviewLabExportMarker_<hash>): Unit  // body is null
 * ```
 *
 * After:
 * ```kotlin
 * public fun previewLabExport(value: PreviewLabExportMarker_<hash>): Unit { /* empty */ }
 * ```
 */
internal class PreviewLabHintIrBodyFiller(private val pluginContext: IrPluginContext,) : IrElementTransformerVoid() {

    /**
     * Detects whether [origin] was produced by our FIR-side hint generator key
     * ([Keys.PreviewLabHint]) so we own filling its body.
     */
    private fun IrDeclarationOrigin.isPreviewLabFirOrigin(): Boolean {
        if (this !is IrDeclarationOrigin.GeneratedByPlugin) return false
        return pluginKey === Keys.PreviewLabHint
    }

    /**
     * Fills the body of FIR-generated hint functions with an empty `Unit`-returning block.
     *
     * The function carries no observable behavior at runtime — its sole purpose is to occupy
     * a [org.jetbrains.kotlin.ir.util.IdSignature] slot in the published klib so downstream
     * `referenceFunctions(...)` discovers it during cross-module preview aggregation.
     */
    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        if (declaration.body == null && declaration.origin.isPreviewLabFirOrigin()) {
            val builder = DeclarationIrBuilder(pluginContext, declaration.symbol)
            declaration.body = builder.irBlockBody { /* empty */ }
        }
        return super.visitSimpleFunction(declaration)
    }
}
