package me.tbsten.compose.preview.lab.compiler.compat.k240beta2

import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.compat.k230.CompatContextImpl as K230CompatContextImpl
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.impl.IrAnnotationImpl
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET

/**
 * Kotlin 2.4.0-Beta2+ compatibility layer.
 *
 * 2.4 では `IrSimpleFunction.annotations` の要素型が `List<IrConstructorCall>` から
 * `List<IrAnnotation>` に変更された。`IrConstructorCallImpl` は `IrAnnotation` のサブタイプではないため、
 * annotations リストに直接追加するとランタイムで ClassCastException となる。
 * このクラスでは [IrAnnotationImpl] を生成する実装に override する。
 *
 * その他の API ([isFirFunction]) は 2.3 と同じ仕様なので [K230CompatContextImpl] に委譲する。
 */
public class CompatContextImpl : CompatContext by K230CompatContextImpl() {

    override fun addConstructorCallAnnotation(
        function: IrSimpleFunction,
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
        function.annotations = function.annotations + annotation
    }

    public class Factory : CompatContext.Factory {
        override val minVersion: String = "2.4.0-Beta2"
        override fun create(): CompatContext = CompatContextImpl()
    }
}
