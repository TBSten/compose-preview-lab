package me.tbsten.compose.preview.lab.compiler.compat

import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall

/**
 * Kotlin 2.4+ compatibility layer.
 */

/** Check if a FIR declaration is a function. In 2.4, FirSimpleFunction was merged into FirFunction. */
internal fun FirDeclaration.isFirFunction(): Boolean = this is FirFunction

/** Get annotations as IrConstructorCall list. In 2.4, annotations type changed to List<IrAnnotation>. */
@Suppress("UNCHECKED_CAST")
internal fun IrSimpleFunction.getAnnotationsList(): List<IrConstructorCall> =
    annotations as List<IrConstructorCall>

/** Set annotations from IrConstructorCall list. */
@Suppress("UNCHECKED_CAST")
internal fun IrSimpleFunction.setAnnotationsList(annotations: List<IrConstructorCall>) {
    this.annotations = annotations as List<Nothing>
}
