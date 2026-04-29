package me.tbsten.compose.preview.lab.compiler.compat.k2220

import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.compat.k222.CompatContextImpl as K222CompatContextImpl
import org.jetbrains.kotlin.ir.IrFileEntry
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.NaiveSourceBasedFileEntryImpl

/**
 * Compatibility layer for Kotlin 2.2.20 and 2.2.21.
 *
 * - In 2.2.20 the return type of `IrClassifierSymbol.defaultType` was changed from `IrType`
 *   to `IrSimpleType`. The k222 module is compiled against 2.2.0 and therefore emits a call
 *   to the `IrType`-returning variant; running that bytecode under 2.2.20 throws
 *   `NoSuchMethodError`. This module overrides [getDefaultType] using code compiled against
 *   2.2.20 so the correct JVM method descriptor is used at runtime.
 * - In 2.2.20 `NaiveSourceBasedFileEntryImpl` also gained a `firstRelevantLineIndex`
 *   parameter (4 explicit params instead of 3). The k222 implementation calls the 3-param
 *   ctor which doesn't exist in 2.2.20+, so we override [createSyntheticFileEntry] here.
 *
 * All other behaviour is delegated to [K222CompatContextImpl].
 */
public class CompatContextImpl : CompatContext by K222CompatContextImpl() {

    override fun getDefaultType(classSymbol: IrClassSymbol): IrSimpleType = classSymbol.defaultType

    override fun createSyntheticFileEntry(fileName: String): IrFileEntry =
        NaiveSourceBasedFileEntryImpl(fileName, IntArray(0), 0, 0)

    public class Factory : CompatContext.Factory {
        override val minVersion: String = "2.2.20"
        override fun create(): CompatContext = CompatContextImpl()
    }
}
