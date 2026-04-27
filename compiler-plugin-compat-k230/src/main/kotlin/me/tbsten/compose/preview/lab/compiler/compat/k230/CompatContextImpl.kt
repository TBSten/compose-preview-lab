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
 * Kotlin 2.3.x compatibility layer.
 *
 * - FIR: 2.3 で `FirSimpleFunction` が `FirFunction` に統合されたため、`FirFunction` で判定する。
 * - IR: 2.3 では `IrSimpleFunction.annotations` の要素型がまだ `List<IrConstructorCall>` なので、
 *       `IrConstructorCallImpl` を直接 annotations に追加できる。
 *
 * 2.3.0 / 2.3.10 / 2.3.20 / 2.3.21 の patch 間でこれら API は同じ。
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
