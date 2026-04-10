package me.tbsten.compose.preview.lab.compiler.compat

import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET

/**
 * Kotlin 2.2 以前の compatibility layer。
 *
 * FIR では FirSimpleFunction がまだ FirFunction と別個のクラスなので、
 * `isFirFunction()` のチェック対象が異なる。
 */

/** Check if a FIR declaration is a function (Kotlin 2.2/以前: FirSimpleFunction). */
internal fun FirDeclaration.isFirFunction(): Boolean = this is FirSimpleFunction

/**
 * 指定された constructor symbol からアノテーションを生成して [IrSimpleFunction] に追加する。
 */
internal fun IrSimpleFunction.addConstructorCallAnnotation(
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
    annotations = annotations + annotation
}
