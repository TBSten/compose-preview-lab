package me.tbsten.compose.preview.lab.compiler.compat.k2220

import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.compat.k222.CompatContextImpl as K222CompatContextImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.defaultType

/**
 * Compatibility layer for Kotlin 2.2.20 and 2.2.21.
 *
 * In 2.2.20 the return type of `IrClassifierSymbol.defaultType` was changed from `IrType`
 * to `IrSimpleType`. The k222 module is compiled against 2.2.0 and therefore emits a call
 * to the `IrType`-returning variant; running that bytecode under 2.2.20 throws
 * `NoSuchMethodError`. This module overrides [getDefaultType] using code compiled against
 * 2.2.20 so the correct JVM method descriptor is used at runtime.
 *
 * All other behaviour is delegated to [K222CompatContextImpl].
 */
public class CompatContextImpl : CompatContext by K222CompatContextImpl() {

    override fun getDefaultType(classSymbol: IrClassSymbol): IrSimpleType = classSymbol.defaultType

    public class Factory : CompatContext.Factory {
        override val minVersion: String = "2.2.20"
        override fun create(): CompatContext = CompatContextImpl()
    }
}
