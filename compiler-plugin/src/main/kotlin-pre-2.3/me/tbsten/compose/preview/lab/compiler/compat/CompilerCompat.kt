package me.tbsten.compose.preview.lab.compiler.compat

import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall

/** Check if a FIR declaration is a function (Kotlin 2.2/2.3: FirSimpleFunction). */
internal fun FirDeclaration.isFirFunction(): Boolean = this is FirSimpleFunction

/** Get annotations as IrConstructorCall list. */
internal fun IrSimpleFunction.getAnnotationsList(): List<IrConstructorCall> = annotations

/** Set annotations from IrConstructorCall list. */
internal fun IrSimpleFunction.setAnnotationsList(annotations: List<IrConstructorCall>) {
    this.annotations = annotations
}
