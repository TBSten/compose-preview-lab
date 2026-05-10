@file:OptIn(org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.feature.previewCollection

import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.lazyResolveToPhase
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.isMarkedNullable
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.isMarkedNullable

// Single source of truth for the parameter-type FQN list that feeds the hint
// canonical key on both the FIR and IR sides. Having one file own the format spec
// (this top-of-file KDoc) keeps the FIR-side `FirNamedFunctionSymbol.parameterTypeFqns()`
// and the IR-side `IrSimpleFunction.parameterTypeFqns()` from drifting — both delegate
// to the same documented shape and any future change updates both extensions in one
// place.
//
// # Format
//
// Each value parameter of the `@Preview` function contributes exactly one entry:
//
// - `<classFqn>` — classId resolved, non-nullable type
// - `<classFqn>?` — classId resolved, nullable type
// - `?` — classId unresolved (generic type parameter or resolution failure;
//   nullability is intentionally ignored in this case to keep the format minimal)
//
// The list ordering matches the function's value-parameter ordering. Joining these
// entries with `,` is what [buildPreviewHintCanonicalKey] does to produce the hash
// input.
//
// Same-name overloads (`fun MyButton()` vs `fun MyButton(text: String)`) yield
// distinct lists and therefore distinct hashes; the marker class can disambiguate
// them in the KLIB IdSignature.

/**
 * Converts a FIR `@Preview` function's value parameter types into the FQN list used in
 * the hint canonical key.
 *
 * Resolution requires the TYPES phase; the call advances the symbol to it via
 * [lazyResolveToPhase] before walking parameters.
 *
 * **Sample call → returned list** (`@Preview fun MyButton(text: String?, count: Int)`):
 * - `parameterTypeFqns()` → `["kotlin.String?", "kotlin.Int"]`
 *
 * Format spec lives in the top-of-file KDoc; this overload and the IR variant share it.
 */
internal fun FirNamedFunctionSymbol.parameterTypeFqns(): List<String> {
    lazyResolveToPhase(FirResolvePhase.TYPES)
    return valueParameterSymbols.map { paramSymbol ->
        val coneType = paramSymbol.resolvedReturnTypeRef.coneType
        val classFqn = coneType.classId?.asFqNameString() ?: return@map "?"
        if (coneType.isMarkedNullable) "$classFqn?" else classFqn
    }
}

/**
 * Converts an IR `@Preview` function's value parameter types into the FQN list used in
 * the hint canonical key. Same format as [FirNamedFunctionSymbol.parameterTypeFqns];
 * format spec lives in the top-of-file KDoc.
 *
 * **Sample call → returned list** (`@Preview fun MyButton(text: String?, count: Int)`):
 * - `parameterTypeFqns()` → `["kotlin.String?", "kotlin.Int"]`
 */
internal fun IrSimpleFunction.parameterTypeFqns(): List<String> = parameters
    .filter { it.kind == IrParameterKind.Regular }
    .map { param ->
        val type = param.type
        val classFqn = type.classFqName?.asString() ?: return@map "?"
        if (type.isMarkedNullable()) "$classFqn?" else classFqn
    }
