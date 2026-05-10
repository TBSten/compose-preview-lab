@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.feature.previewCollection.ir.collectPreviewsReplacement.buildPreviewSequence

import me.tbsten.compose.preview.lab.compiler.compat.IrDeclarationOriginCompat
import me.tbsten.compose.preview.lab.compiler.utils.callableIdOf
import me.tbsten.compose.preview.lab.compiler.utils.classIdOf
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.name.SpecialNames

/**
 * Builds `lazy { valueExpr }` where [valueExpr] has type `Sequence<CollectedPreview>`.
 *
 * **Sample call → resulting IR**:
 * ```kotlin
 * BuildLazyWrapperIr(context)
 *     .invoke(builder, sequenceExpr, parent)
 * // result IR ≡  lazy<Sequence<CollectedPreview>> { sequenceExpr }
 * ```
 *
 * The lazy is needed because [valueExpr] is itself the `lazyPreviewSequence({...}, ...)`
 * call: while each `CollectedPreview` inside the sequence is lazily realized per-element,
 * the *act* of looking up `lazyPreviewSequence` and constructing the factory-lambda array
 * still runs at the first `getValue`. Wrapping in `lazy` defers that one-shot setup to
 * the property's first access.
 *
 * Class shares the [PreviewSequenceBuildContext] with [BuildPreviewSequenceIr] so the
 * `Sequence<CollectedPreview>` type lookup is cached.
 */
internal class BuildLazyWrapperIr(private val context: PreviewSequenceBuildContext) {

    operator fun invoke(
        builder: DeclarationIrBuilder,
        valueExpr: IrExpression,
        declarationParent: IrDeclarationParent,
    ): IrExpression {
        val lazyFun = context.pluginContext.referenceFunctions(
            callableIdOf("kotlin", "lazy"),
        ).first { fn ->
            fn.owner.parameters.filter { it.kind == IrParameterKind.Regular }.size == 1
        }

        val lambdaFun = context.pluginContext.irFactory.buildFun {
            startOffset = SYNTHETIC_OFFSET
            endOffset = SYNTHETIC_OFFSET
            name = SpecialNames.ANONYMOUS
            returnType = context.sequenceOfCollectedPreviewType
            origin = IrDeclarationOriginCompat.LOCAL_FUNCTION_FOR_LAMBDA
            visibility = DescriptorVisibilities.LOCAL
        }.apply {
            parent = declarationParent
            body = DeclarationIrBuilder(context.pluginContext, symbol).irBlockBody {
                +irReturn(valueExpr)
            }
        }

        val lambdaType = context.pluginContext.irBuiltIns.functionN(0)
            .typeWith(context.sequenceOfCollectedPreviewType)

        val lambdaExpr = IrFunctionExpressionImpl(
            startOffset = SYNTHETIC_OFFSET,
            endOffset = SYNTHETIC_OFFSET,
            type = lambdaType,
            origin = IrStatementOrigin.LAMBDA,
            function = lambdaFun,
        )

        return context.compatContext.irCall(
            builder,
            lazyFun,
            context.pluginContext.referenceClass(
                classIdOf("kotlin", "Lazy"),
            )!!.typeWith(context.sequenceOfCollectedPreviewType),
            listOf(context.sequenceOfCollectedPreviewType),
        ).apply {
            arguments[0] = lambdaExpr
        }
    }
}
