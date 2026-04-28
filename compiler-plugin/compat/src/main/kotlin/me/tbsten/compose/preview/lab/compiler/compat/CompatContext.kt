package me.tbsten.compose.preview.lab.compiler.compat

import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType

/**
 * SPI that absorbs version-specific differences in the Kotlin compiler API.
 *
 * Implementations are registered as a [Factory] in
 * `META-INF/services/.../CompatContext$Factory`, and [CompatContextLoader]
 * picks the best one at runtime based on the current Kotlin compiler version.
 *
 * When a new Kotlin version introduces API drift, add an abstract method here
 * and implement it in each compat module ([Factory.minVersion]).
 */
public interface CompatContext {
    /**
     * Returns whether the FIR declaration is a function.
     *
     * The concrete FIR type used to represent functions has changed across Kotlin versions
     * (e.g. `FirSimpleFunction` vs `FirFunction`); this method abstracts that away.
     */
    public fun isFirFunction(declaration: FirDeclaration): Boolean

    /**
     * Returns the default type of a class symbol.
     *
     * `IrClassifierSymbol.defaultType` (extension) was introduced in 2.2.20; earlier versions
     * only have `IrClass.defaultType` as a direct member property.
     */
    public fun getDefaultType(classSymbol: IrClassSymbol): IrSimpleType

    /**
     * Builds an annotation from the given constructor symbol and adds it to [function].
     *
     * The IR annotation element type used in [IrSimpleFunction.annotations] has changed
     * across Kotlin versions; this method abstracts the correct construction.
     */
    public fun addConstructorCallAnnotation(function: IrSimpleFunction, type: IrType, constructorSymbol: IrConstructorSymbol)

    /**
     * Factory for compat implementations. Each k* module registers its implementation
     * (with its own [minVersion]) in
     * `META-INF/services/me.tbsten.compose.preview.lab.compiler.compat.CompatContext${'$'}Factory`.
     */
    public interface Factory {
        /** The minimum Kotlin version this implementation supports (e.g. "2.3.0", "2.4.0-Beta2"). */
        public val minVersion: String

        public fun create(): CompatContext
    }

    public companion object {
        /**
         * Loads the [CompatContext] implementation that best matches the current Kotlin compiler version.
         *
         * @param knownVersion for tests / explicit selection. When null, detected automatically
         *                     from META-INF/compiler.version.
         */
        public fun load(knownVersion: KotlinToolingVersion? = null): CompatContext = CompatContextLoader.load(knownVersion)
    }
}
