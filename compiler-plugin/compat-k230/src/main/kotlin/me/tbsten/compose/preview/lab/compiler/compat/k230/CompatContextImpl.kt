package me.tbsten.compose.preview.lab.compiler.compat.k230

import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import org.jetbrains.kotlin.fir.FirAnnotationContainer
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.DeprecationsProvider
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.getBooleanArgument
import org.jetbrains.kotlin.fir.declarations.getDeprecationsProvider
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueDeclaration
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.IrFileEntry
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.util.NaiveSourceBasedFileEntryImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

/**
 * Compatibility layer for Kotlin 2.3.x.
 *
 * - FIR: in 2.3 `FirSimpleFunction` was merged into `FirFunction`, so we check against
 *   `FirFunction`.
 * - IR: in 2.3 the element type of `IrSimpleFunction.annotations` is still
 *   `List<IrConstructorCall>`, so `IrConstructorCallImpl` can be appended directly.
 * - IR builders: by 2.3.x the `irCall` / `irGet` / `irString` helpers accept the wider
 *   `IrBuilder` receiver, but `IrBuilderWithScope` is still a subtype, so the same call
 *   sites resolve correctly. This module compiles them against 2.3.x bytecode.
 *
 * These APIs are identical across the 2.3.0 / 2.3.10 / 2.3.20 / 2.3.21 patches.
 */
public class CompatContextImpl : CompatContext {

    override fun isFirFunction(declaration: FirDeclaration): Boolean = declaration is FirFunction

    override fun getDefaultType(classSymbol: IrClassSymbol): IrSimpleType = classSymbol.defaultType

    override fun addConstructorCallAnnotation(
        function: IrSimpleFunction,
        type: IrType,
        constructorSymbol: IrConstructorSymbol,
    ) {
        val annotation = IrConstructorCallImpl(
            startOffset = SYNTHETIC_OFFSET,
            endOffset = SYNTHETIC_OFFSET,
            type = type,
            symbol = constructorSymbol,
            typeArgumentsCount = 0,
            constructorTypeArgumentsCount = 0,
        )
        function.annotations = function.annotations + annotation
    }

    override fun irCall(builder: IrBuilderWithScope, callee: IrSimpleFunctionSymbol): IrCall = builder.irCall(callee)

    override fun irCall(builder: IrBuilderWithScope, callee: IrFunctionSymbol, returnType: IrType): IrFunctionAccessExpression =
        builder.irCall(callee, returnType)

    override fun irCall(
        builder: IrBuilderWithScope,
        callee: IrFunctionSymbol,
        returnType: IrType,
        typeArguments: List<IrType>,
    ): IrMemberAccessExpression<*> = builder.irCall(callee, returnType, typeArguments)

    override fun irGet(builder: IrBuilderWithScope, variable: IrValueDeclaration): IrGetValue = builder.irGet(variable)

    override fun irString(builder: IrBuilderWithScope, value: String): IrExpression = builder.irString(value)

    // Kotlin 2.3.x: NaiveSourceBasedFileEntryImpl gained the `firstRelevantLineIndex` parameter
    // (4 explicit params instead of 3). This module compiles against 2.3.x so the bytecode
    // emits the matching JVM signature.
    override fun createSyntheticFileEntry(fileName: String): IrFileEntry =
        NaiveSourceBasedFileEntryImpl(fileName, IntArray(0), 0, 0)

    override fun transformModuleFragment(moduleFragment: IrModuleFragment, transformer: Any) {
        moduleFragment.transform(transformer as IrElementTransformerVoid, null)
    }

    // Kotlin 2.3.0–2.3.20: KT-82395 (JS/Wasm IC × top-level decl gen) is still open, so the
    // FIR-based hint generator cannot run safely. The 2.3.21 patch (compat-k2321) flips this on.
    override fun supportsKlibCrossModuleHint(): Boolean = false

    // 2.3.x: same `FirAnnotationContainer.getDeprecationsProvider(session)` extension as 2.1 / 2.2.
    override fun getDeprecationsProviderCompat(
        declaration: FirAnnotationContainer,
        session: FirSession,
    ): DeprecationsProvider = declaration.getDeprecationsProvider(session)

    // Pre-2.4: stdlib helper requires `(name, session)` to read the resolved mapping.
    override fun getBooleanArgumentCompat(annotation: FirAnnotation, name: Name, session: FirSession,): Boolean? =
        annotation.getBooleanArgument(name, session)

    public class Factory : CompatContext.Factory {
        override val minVersion: String = "2.3.0"
        override fun create(): CompatContext = CompatContextImpl()
    }
}
