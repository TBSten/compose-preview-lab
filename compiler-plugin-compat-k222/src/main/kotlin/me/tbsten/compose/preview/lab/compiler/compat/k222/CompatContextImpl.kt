package me.tbsten.compose.preview.lab.compiler.compat.k222

import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET

/**
 * Compatibility layer for Kotlin 2.2.x.
 *
 * - FIR: in 2.2 functions are represented as [FirSimpleFunction] (not [FirFunction], which was
 *   introduced in 2.3 as a merged supertype). We check against [FirSimpleFunction] here.
 * - IR: [IrConstructorCallImpl] is still the correct annotation element type in 2.2.
 */
public class CompatContextImpl : CompatContext {

    override fun isFirFunction(declaration: FirDeclaration): Boolean = declaration is FirSimpleFunction

    // In 2.2.x IrClassifierSymbol.defaultType returns IrType; cast is safe for class symbols.
    @Suppress("UNCHECKED_CAST")
    override fun getDefaultType(classSymbol: IrClassSymbol): IrSimpleType = classSymbol.defaultType as IrSimpleType

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
        override val minVersion: String = "2.2.0"
        override fun create(): CompatContext = CompatContextImpl()
    }
}
