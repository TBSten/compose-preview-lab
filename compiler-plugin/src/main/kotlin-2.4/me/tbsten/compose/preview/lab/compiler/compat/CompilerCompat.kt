package me.tbsten.compose.preview.lab.compiler.compat

import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.impl.IrAnnotationImpl
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET

/**
 * Kotlin 2.4+ compatibility layer.
 *
 * 2.4 では `IrSimpleFunction.annotations` の要素型が `List<IrConstructorCall>` から
 * `List<IrAnnotation>` に変更された。IrAnnotation は IrConstructorCall のサブクラスだが、
 * IrConstructorCallImpl は IrAnnotation のサブタイプではないため、annotations リストに
 * IrConstructorCallImpl を直接追加するとランタイムで ClassCastException となる。
 * 本ファイルでは IrAnnotationImpl を生成する実装を提供する。
 */

/** Check if a FIR declaration is a function. 2.4 では FirSimpleFunction が FirFunction に統合された。 */
internal fun FirDeclaration.isFirFunction(): Boolean = this is FirFunction

/**
 * 指定された constructor symbol からアノテーションを生成して [IrSimpleFunction] に追加する。
 *
 * 2.4 では IrAnnotationImpl を使う必要がある。
 */
internal fun IrSimpleFunction.addConstructorCallAnnotation(
    type: IrType,
    constructorSymbol: IrConstructorSymbol,
) {
    val annotation = IrAnnotationImpl(
        startOffset = SYNTHETIC_OFFSET,
        endOffset = SYNTHETIC_OFFSET,
        type = type,
        symbol = constructorSymbol,
        typeArgumentsCount = 0,
        constructorTypeArgumentsCount = 0,
    )
    annotations = annotations + annotation
}
