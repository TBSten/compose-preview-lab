package me.tbsten.compose.preview.lab.compiler.compat

import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.types.IrType

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
 * Builds an annotation with constructor [arguments] and adds it to the function.
 * See [CompatContext.addConstructorCallAnnotationWithArgs].
 */
internal fun IrSimpleFunction.addConstructorCallAnnotationWithArgs(
    type: IrType,
    constructorSymbol: IrConstructorSymbol,
    arguments: List<IrExpression>,
) = compatContext.addConstructorCallAnnotationWithArgs(this, type, constructorSymbol, arguments)
