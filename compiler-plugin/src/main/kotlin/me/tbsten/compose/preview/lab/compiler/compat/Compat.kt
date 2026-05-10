package me.tbsten.compose.preview.lab.compiler.compat

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.name.Name

/**
 * Access point for the version-specific Kotlin compiler API.
 *
 * The actual differences live in compat modules that implement [CompatContext]
 * (`compiler-plugin/compat-k230`, `compiler-plugin/compat-k240_beta2`, ...).
 * At runtime, ServiceLoader picks the one that matches the current Kotlin compiler.
 *
 * The extension-function shapes here intentionally mirror the ones that used to live
 * in the per-version sourceSets (`kotlin-2.3/`, ...) so call sites do not need to change.
 */
private val compatContext: CompatContext by lazy { CompatContext.load() }

/** Returns whether the FIR declaration is a function. See [CompatContext.isFirFunction]. */
internal fun FirDeclaration.isFirFunction(): Boolean = compatContext.isFirFunction(this)

/**
 * Builds an annotation from the given constructor symbol and adds it to the function.
 * See [CompatContext.addConstructorCallAnnotation].
 */
internal fun IrSimpleFunction.addConstructorCallAnnotation(type: IrType, constructorSymbol: IrConstructorSymbol) =
    compatContext.addConstructorCallAnnotation(this, type, constructorSymbol)

/**
 * Returns the resolved `Boolean` argument named [name] from this annotation, or `null` when
 * the argument is absent. See [CompatContext.getBooleanArgumentCompat].
 */
internal fun FirAnnotation.getBooleanArgumentCompat(name: Name, session: FirSession): Boolean? =
    compatContext.getBooleanArgumentCompat(this, name, session)
