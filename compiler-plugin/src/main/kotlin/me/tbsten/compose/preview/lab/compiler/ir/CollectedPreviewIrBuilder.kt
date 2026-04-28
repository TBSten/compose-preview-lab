@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.ir

import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.compat.IrDeclarationOriginCompat
import me.tbsten.compose.preview.lab.compiler.compat.addConstructorCallAnnotation
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

/**
 * Builds the IR for individual [CollectedPreview] instances.
 */
internal class CollectedPreviewIrBuilder(
    private val pluginContext: IrPluginContext,
    private val compatContext: CompatContext,
) {
    val collectedPreviewClass by lazy {
        pluginContext.referenceClass(
            ClassId(FqName("me.tbsten.compose.preview.lab"), Name.identifier("CollectedPreview")),
        )!!
    }
    val collectedPreviewType by lazy { compatContext.getDefaultType(collectedPreviewClass) }

    /**
     * Builds the IR for `CollectedPreview(id, displayName, ...) { previewFun() }`.
     */
    fun buildCollectedPreviewCall(
        preview: PreviewFunctionInfo,
        builder: DeclarationIrBuilder,
        parent: IrDeclarationParent,
    ): IrExpression {
        val ctor = collectedPreviewClass.constructors.first()
        val contentParamType: IrType = ctor.owner.parameters
            .filter { it.kind == IrParameterKind.Regular }
            .last().type
        val contentLambda = buildContentLambda(preview.function, contentParamType, parent)

        return IrConstructorCallImpl(
            startOffset = SYNTHETIC_OFFSET,
            endOffset = SYNTHETIC_OFFSET,
            type = collectedPreviewType,
            symbol = ctor,
            typeArgumentsCount = 0,
            constructorTypeArgumentsCount = 0,
        ).apply {
            val nullableString = pluginContext.irBuiltIns.stringType.makeNullable()
            val nullableInt = pluginContext.irBuiltIns.intType.makeNullable()
            arguments[0] = compatContext.irString(builder, preview.id)
            arguments[1] = compatContext.irString(builder, preview.displayName)
            arguments[2] = preview.filePath
                ?.let { compatContext.irString(builder, it) }
                ?: IrConstImpl.constNull(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, nullableString)
            arguments[3] = preview.startLineNumber
                ?.let { IrConstImpl.int(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, pluginContext.irBuiltIns.intType, it) }
                ?: IrConstImpl.constNull(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, nullableInt)
            arguments[4] = preview.endLineNumber
                ?.let { IrConstImpl.int(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, pluginContext.irBuiltIns.intType, it) }
                ?: IrConstImpl.constNull(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, nullableInt)
            arguments[5] = preview.code
                ?.let { compatContext.irString(builder, it) }
                ?: IrConstImpl.constNull(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, nullableString)
            arguments[6] = preview.kdoc
                ?.let { compatContext.irString(builder, it) }
                ?: IrConstImpl.constNull(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, nullableString)
            arguments[7] = contentLambda
        }
    }

    /**
     * Builds the @Composable IR lambda for `{ previewFun() }`.
     */
    private fun buildContentLambda(
        previewFunc: IrSimpleFunction,
        contentType: IrType,
        parent: IrDeclarationParent,
    ): IrFunctionExpressionImpl {
        val lambdaFun = pluginContext.irFactory.buildFun {
            startOffset = SYNTHETIC_OFFSET
            endOffset = SYNTHETIC_OFFSET
            name = SpecialNames.ANONYMOUS
            returnType = pluginContext.irBuiltIns.unitType
            origin = IrDeclarationOriginCompat.LOCAL_FUNCTION_FOR_LAMBDA
            visibility = DescriptorVisibilities.LOCAL
        }.also { lambda ->
            lambda.parent = parent
            addComposableAnnotationIfAvailable(lambda)
            lambda.body = DeclarationIrBuilder(pluginContext, lambda.symbol).irBlockBody {
                +irReturn(compatContext.irCall(this, previewFunc.symbol))
            }
        }

        return IrFunctionExpressionImpl(
            startOffset = SYNTHETIC_OFFSET,
            endOffset = SYNTHETIC_OFFSET,
            type = contentType,
            origin = IrStatementOrigin.LAMBDA,
            function = lambdaFun,
        )
    }

    private fun addComposableAnnotationIfAvailable(func: IrSimpleFunction) {
        val composableClassId = ClassId(
            FqName("androidx.compose.runtime"),
            Name.identifier("Composable"),
        )
        val composableClass = pluginContext.referenceClass(composableClassId) ?: return
        val ctor = composableClass.constructors.firstOrNull() ?: return
        func.addConstructorCallAnnotation(
            type = compatContext.getDefaultType(composableClass),
            constructorSymbol = ctor,
        )
    }
}
