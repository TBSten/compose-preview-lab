package me.tbsten.compose.preview.lab.compiler.compat

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.name.Name

/**
 * Returns whether the FIR declaration is a function. See [CompatContext.isFirFunction].
 *
 * Resolves the version-specific [CompatContext] via [FirSession.compatContext], so callers
 * only need the FIR session in scope.
 */
internal fun FirDeclaration.isFirFunction(session: FirSession): Boolean = session.compatContext.isFirFunction(this)

/**
 * Builds an annotation from the given constructor symbol and adds it to the function.
 * See [CompatContext.addConstructorCallAnnotation].
 *
 * IR-side: no FIR session in scope, so the [compatContext] is passed explicitly (= bucket
 * relay continues on the IR side, by design — see the registrar for the entry-point load).
 */
internal fun IrSimpleFunction.addConstructorCallAnnotation(
    compatContext: CompatContext,
    type: IrType,
    constructorSymbol: IrConstructorSymbol,
) = compatContext.addConstructorCallAnnotation(this, type, constructorSymbol)

/**
 * Returns the resolved `Boolean` argument named [name] from this annotation, or `null` when
 * the argument is absent or unresolvable. See [CompatContext.getBooleanArgumentCompat].
 *
 * Resolves [CompatContext] via [FirSession.compatContext] from the supplied [session].
 */
internal fun FirAnnotation.getBooleanArgumentCompat(name: Name, session: FirSession): Boolean? =
    session.compatContext.getBooleanArgumentCompat(this, name, session)
