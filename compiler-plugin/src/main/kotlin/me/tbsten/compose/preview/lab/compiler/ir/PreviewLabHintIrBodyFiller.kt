@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.ir

import me.tbsten.compose.preview.lab.compiler.fir.Keys
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irDelegatingConstructorCall
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

/**
 * Injects empty bodies into the synthetic declarations the FIR-side
 * [me.tbsten.compose.preview.lab.compiler.fir.PreviewLabHintFirGenerator] emits.
 *
 * FIR `DeclarationGenerationExtension` cannot produce `IrBody` (bodies are an IR-only
 * concept), so the FIR generator hands the IR pass an empty constructor and an empty
 * hint function and relies on this transformer to fill them in. Without these bodies
 * the JVM backend asserts on
 * `IllegalStateException: Function has no body: CONSTRUCTOR GENERATED[...]`.
 *
 * **Marker class constructor — Before**:
 * ```kotlin
 * package me.tbsten.compose.preview.lab.exports
 * public class PreviewLabExportMarker_<hash> public constructor() // body is null
 * ```
 *
 * **After**:
 * ```kotlin
 * public class PreviewLabExportMarker_<hash> public constructor() {
 *     // generated body
 *     super<Any>()
 *     <init class>()  // IrInstanceInitializerCall
 * }
 * ```
 *
 * **Hint function — Before**:
 * ```kotlin
 * public fun previewLabExport(value: PreviewLabExportMarker_<hash>): Unit  // body is null
 * ```
 *
 * **After**:
 * ```kotlin
 * public fun previewLabExport(value: PreviewLabExportMarker_<hash>): Unit { /* empty */ }
 * ```
 */
internal class PreviewLabHintIrBodyFiller(private val pluginContext: IrPluginContext,) : IrElementTransformerVoid() {

    /**
     * Detects whether [origin] was produced by one of our FIR-side generated declaration keys
     * and returns true so we own filling its body. Both
     * [Keys.PreviewLabHintMarker] (constructor) and [Keys.PreviewLabHint] (hint function)
     * surface here as
     * [IrDeclarationOrigin.GeneratedByPlugin] wrapping the corresponding FIR key.
     */
    private fun IrDeclarationOrigin.isPreviewLabFirOrigin(): Boolean {
        if (this !is IrDeclarationOrigin.GeneratedByPlugin) return false
        val key = pluginKey
        return key === Keys.PreviewLabHint || key === Keys.PreviewLabHintMarker
    }

    /**
     * Fills the body of FIR-generated marker class primary constructors with the standard
     * delegating-constructor + instance-initializer pair, equivalent to the body the Kotlin
     * frontend would synthesize for `class Foo()` source.
     */
    override fun visitConstructor(declaration: IrConstructor): IrStatement {
        if (declaration.body == null && declaration.origin.isPreviewLabFirOrigin()) {
            val parentClass = declaration.parentAsClass
            val anyClass = pluginContext.irBuiltIns.anyClass.owner
            val anyCtor = anyClass.declarations
                .filterIsInstance<IrConstructor>()
                .single { it.parameters.isEmpty() }
            val builder = DeclarationIrBuilder(pluginContext, declaration.symbol)
            declaration.body = builder.irBlockBody {
                +irDelegatingConstructorCall(anyCtor)
                +IrInstanceInitializerCallImpl(
                    SYNTHETIC_OFFSET,
                    SYNTHETIC_OFFSET,
                    parentClass.symbol,
                    pluginContext.irBuiltIns.unitType,
                )
            }
        }
        return super.visitConstructor(declaration)
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
