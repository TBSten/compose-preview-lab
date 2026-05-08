package me.tbsten.compose.preview.lab.compiler.compat.k240beta2

import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.compat.k230.CompatContextImpl as K230CompatContextImpl
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.impl.IrAnnotationImpl
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET

/**
 * Compatibility layer for Kotlin 2.4.0-Beta2 and later.
 *
 * In 2.4 the element type of `IrSimpleFunction.annotations` was changed from
 * `List<IrConstructorCall>` to `List<IrAnnotation>`. `IrConstructorCallImpl` is not a
 * subtype of `IrAnnotation`, so appending it directly throws `ClassCastException` at
 * runtime. This class overrides annotation construction to use [IrAnnotationImpl].
 *
 * Other APIs (such as [isFirFunction]) behave the same as in 2.3, so we delegate to
 * [K230CompatContextImpl] for them.
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

    // Kotlin 2.4.0-Beta2+: FIR top-level decl gen is stable on KLIB and KT-82395 is fixed.
    override fun supportsKlibCrossModuleHint(): Boolean = true

    public class Factory : CompatContext.Factory {
        override val minVersion: String = "2.4.0-Beta2"
        override fun create(): CompatContext = CompatContextImpl()
    }
}
