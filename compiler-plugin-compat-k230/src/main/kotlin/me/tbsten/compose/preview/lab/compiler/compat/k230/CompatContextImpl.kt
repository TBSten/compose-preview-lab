package me.tbsten.compose.preview.lab.compiler.compat.k230

import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET

/**
 * Compatibility layer for Kotlin 2.3.x.
 *
 * - FIR: in 2.3 `FirSimpleFunction` was merged into `FirFunction`, so we check against
 *   `FirFunction`.
 * - IR: in 2.3 the element type of `IrSimpleFunction.annotations` is still
 *   `List<IrConstructorCall>`, so `IrConstructorCallImpl` can be appended directly.
 *
 * These APIs are identical across the 2.3.0 / 2.3.10 / 2.3.20 / 2.3.21 patches.
 */
public class CompatContextImpl : CompatContext {

    override fun isFirFunction(declaration: FirDeclaration): Boolean = declaration is FirFunction

    override fun addConstructorCallAnnotation(
        function: IrSimpleFunction,
        type: IrType,
        constructorSymbol: IrConstructorSymbol,
    ) {
        val annotation = IrConstructorCallImpl(
            startOffset = SYNTHETIC_OFFSET,
            endOffset = SYNTHETIC_OFFSET,
            type = type,
            symbol = constructorSymbol,
            typeArgumentsCount = 0,
            constructorTypeArgumentsCount = 0,
        )
        function.annotations = function.annotations + annotation
    }

    public class Factory : CompatContext.Factory {
        override val minVersion: String = "2.3.0"
        override fun create(): CompatContext = CompatContextImpl()
    }
}
