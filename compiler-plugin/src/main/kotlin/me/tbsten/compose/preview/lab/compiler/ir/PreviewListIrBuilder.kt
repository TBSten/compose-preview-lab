@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.ir

import me.tbsten.compose.preview.lab.compiler.PluginConfig
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irTemporary
import me.tbsten.compose.preview.lab.compiler.compat.IrDeclarationOriginCompat
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

/**
 * Builds the IR for the full preview list.
 *
 * - `listOf(CollectedPreview(...), ...)` / `emptyList()` construction
 * - `lazy { ... }` wrapping
 * - cross-module concatenation (`mutableListOf().apply { addAll(...) }`)
 */
internal class PreviewListIrBuilder(
    private val pluginContext: IrPluginContext,
    private val previews: List<PreviewFunctionInfo>,
    private val config: PluginConfig,
) {
    private val previewBuilder = CollectedPreviewIrBuilder(pluginContext)

    private val collectedPreviewType get() = previewBuilder.collectedPreviewType

    private val listOfCollectedPreviewType by lazy {
        pluginContext.referenceClass(
            ClassId(FqName("kotlin.collections"), Name.identifier("List")),
        )!!.typeWith(collectedPreviewType)
    }

    // ----- Preview list construction -----

    /** Builds either `listOf(CollectedPreview(...), ...)` or `emptyList()`. */
    fun buildPreviewsListExpr(builder: DeclarationIrBuilder, parent: IrDeclarationParent): IrExpression {
        if (previews.isEmpty()) return buildEmptyListCall(builder)

        val listOfFun = pluginContext.referenceFunctions(
            CallableId(FqName("kotlin.collections"), Name.identifier("listOf")),
        ).first { fn ->
            val valueParams = fn.owner.parameters.filter { it.kind == IrParameterKind.Regular }
            valueParams.size == 1 && valueParams[0].varargElementType != null
        }

        val elements = previews.map { previewBuilder.buildCollectedPreviewCall(it, builder, parent) }

        val vararg: IrVararg = IrVarargImpl(
            startOffset = SYNTHETIC_OFFSET,
            endOffset = SYNTHETIC_OFFSET,
            type = pluginContext.irBuiltIns.arrayClass.typeWith(collectedPreviewType),
            varargElementType = collectedPreviewType,
            elements = elements.toMutableList(),
        )

        return builder.irCall(listOfFun, listOfCollectedPreviewType, listOf(collectedPreviewType)).apply {
            arguments[0] = vararg
        }
    }

    private fun buildEmptyListCall(builder: IrBuilderWithScope): IrExpression {
        val emptyListFun = pluginContext.referenceFunctions(
            CallableId(FqName("kotlin.collections"), Name.identifier("emptyList")),
        ).first()
        return builder.irCall(emptyListFun, listOfCollectedPreviewType, listOf(collectedPreviewType))
    }

    // ----- Lazy wrapper -----

    /** Builds the IR for `lazy { valueExpr }`. */
    fun buildLazyCall(builder: DeclarationIrBuilder, valueExpr: IrExpression, parent: IrDeclarationParent,): IrExpression {
        val lazyFun = pluginContext.referenceFunctions(
            CallableId(FqName("kotlin"), Name.identifier("lazy")),
        ).first { fn ->
            fn.owner.parameters.filter { it.kind == IrParameterKind.Regular }.size == 1
        }

        val lambdaFun = pluginContext.irFactory.buildFun {
            startOffset = SYNTHETIC_OFFSET
            endOffset = SYNTHETIC_OFFSET
            name = SpecialNames.ANONYMOUS
            returnType = listOfCollectedPreviewType
            origin = IrDeclarationOriginCompat.LOCAL_FUNCTION_FOR_LAMBDA
            visibility = DescriptorVisibilities.LOCAL
        }.also { lambda ->
            lambda.parent = parent
            lambda.body = DeclarationIrBuilder(pluginContext, lambda.symbol).irBlockBody {
                +irReturn(valueExpr)
            }
        }

        val lambdaType = pluginContext.irBuiltIns.functionN(0).typeWith(listOfCollectedPreviewType)

        val lambdaExpr = IrFunctionExpressionImpl(
            startOffset = SYNTHETIC_OFFSET,
            endOffset = SYNTHETIC_OFFSET,
            type = lambdaType,
            origin = IrStatementOrigin.LAMBDA,
            function = lambdaFun,
        )

        return builder.irCall(
            lazyFun,
            pluginContext.referenceClass(
                ClassId(FqName("kotlin"), Name.identifier("Lazy")),
            )!!.typeWith(listOfCollectedPreviewType),
            listOf(listOfCollectedPreviewType),
        ).apply {
            arguments[0] = lambdaExpr
        }
    }

    // ----- Cross-module concatenation -----

    /**
     * Builds an expression that concatenates this module's previews with previews from
     * dependency modules.
     *
     * Generates `mutableListOf<CollectedPreview>().apply { addAll(this); addAll(dep) }`.
     */
    fun buildConcatenatedPreviewsExpr(builder: DeclarationIrBuilder, thisModulePreviews: IrExpression,): IrExpression {
        val depProperties = collectDependencyProperties()
        if (depProperties.isEmpty()) return thisModulePreviews

        val mutableListOfFun = pluginContext.referenceFunctions(
            CallableId(FqName("kotlin.collections"), Name.identifier("mutableListOf")),
        ).first { fn ->
            fn.owner.parameters.filter { it.kind == IrParameterKind.Regular }.isEmpty()
        }

        val addAllFun = pluginContext.referenceFunctions(
            CallableId(FqName("kotlin.collections"), Name.identifier("addAll")),
        ).first { fn ->
            val params = fn.owner.parameters.filter { it.kind == IrParameterKind.Regular }
            params.size == 1 && params[0].varargElementType == null
        }

        val mutableListType = pluginContext.referenceClass(
            ClassId(FqName("kotlin.collections"), Name.identifier("MutableList")),
        )!!.typeWith(collectedPreviewType)

        return builder.irBlock {
            val listVar = irTemporary(
                builder.irCall(mutableListOfFun, mutableListType, listOf(collectedPreviewType)),
            )
            +irCall(addAllFun, pluginContext.irBuiltIns.booleanType, listOf(collectedPreviewType)).apply {
                arguments[0] = irGet(listVar)
                arguments[1] = thisModulePreviews
            }
            for (depProp in depProperties) {
                val depGetter = depProp.getter ?: continue
                val depValue = builder.irCall(depGetter.symbol, listOfCollectedPreviewType)
                +irCall(addAllFun, pluginContext.irBuiltIns.booleanType, listOf(collectedPreviewType)).apply {
                    arguments[0] = irGet(listVar)
                    arguments[1] = depValue
                }
            }
            +irGet(listVar)
        }
    }

    private fun collectDependencyProperties(): List<IrProperty> = config.dependencyCollectPreviewsFqns.mapNotNull { fqn ->
        val lastDot = fqn.lastIndexOf('.')
        if (lastDot < 0) return@mapNotNull null
        val packageFqName = FqName(fqn.substring(0, lastDot))
        val propertyName = Name.identifier(fqn.substring(lastDot + 1))
        pluginContext.referenceProperties(CallableId(packageFqName, propertyName)).firstOrNull()?.owner
    }
}
