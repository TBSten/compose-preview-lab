package me.tbsten.compose.preview.lab.compiler.compat.k222

import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import org.jetbrains.kotlin.fir.FirAnnotationContainer
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.DeprecationsProvider
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
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
 * Compatibility layer for Kotlin 2.2.x.
 *
 * - FIR: in 2.2 functions are represented as [FirSimpleFunction] (not [FirFunction], which was
 *   introduced in 2.3 as a merged supertype). We check against [FirSimpleFunction] here.
 * - IR: [IrConstructorCallImpl] is still the correct annotation element type in 2.2.
 * - IR builders: in 2.2.x the `irCall` / `irGet` / `irString` helpers take an `IrBuilderWithScope`
 *   receiver. The receiver was widened to `IrBuilder` in later versions, so the bytecode emitted
 *   by this module pins the 2.2.x JVM signature.
 */
public class CompatContextImpl : CompatContext {

    override fun isFirFunction(declaration: FirDeclaration): Boolean = declaration is FirSimpleFunction

    // In 2.2.x IrClassifierSymbol.defaultType returns IrType; cast is safe for class symbols.
    @Suppress("UNCHECKED_CAST")
    override fun getDefaultType(classSymbol: IrClassSymbol): IrSimpleType = classSymbol.defaultType as IrSimpleType

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

    // Kotlin 2.2.x: NaiveSourceBasedFileEntryImpl(name, lineStartOffsets, maxOffset).
    override fun createSyntheticFileEntry(fileName: String): IrFileEntry =
        NaiveSourceBasedFileEntryImpl(fileName, IntArray(0), 0)

    override fun transformModuleFragment(moduleFragment: IrModuleFragment, transformer: Any) {
        moduleFragment.transform(transformer as IrElementTransformerVoid, null)
    }

    // Kotlin 2.2.x: `FirDeclarationGenerationExtension.getTopLevelClassIds` /
    // `getTopLevelCallableIds` were not stable enough to register the per-`@Preview`
    // hint generator without crashing the FIR session. Both gates are off.
    override fun supportsFirHintGeneration(): Boolean = false

    override fun supportsKlibCrossModuleHint(): Boolean = false

    // Kotlin 2.2.x: `org.jetbrains.kotlin.fir.declarations.FirNamedFunction` does not
    // exist yet (introduced in 2.3.20). Skip checker registration so the JVM classloader
    // never tries to load `PreviewLabFirCheckersExtension` and we avoid
    // `NoClassDefFoundError` at plugin startup.
    override fun supportsFirCheckers(): Boolean = false

    // 2.2.x: same `FirAnnotationContainer.getDeprecationsProvider(session)` extension as 2.1.
    override fun getDeprecationsProviderCompat(
        declaration: FirAnnotationContainer,
        session: FirSession,
    ): DeprecationsProvider = declaration.getDeprecationsProvider(session)

    // Pre-2.4: stdlib helper requires `(name, session)` to read the resolved mapping.
    override fun getBooleanArgumentCompat(annotation: FirAnnotation, name: Name, session: FirSession,): Boolean? =
        annotation.getBooleanArgument(name, session)

    public class Factory : CompatContext.Factory {
        override val minVersion: String = "2.2.0"
        override fun create(): CompatContext = CompatContextImpl()
    }
}
