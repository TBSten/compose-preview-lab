package me.tbsten.compose.preview.lab.compiler.ir

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.declarations.addField
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrReturnImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.expressions.impl.fromSymbolOwner
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.createImplicitParameterDeclarationWithWrappedDescriptor
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

internal class PreviewListIrGenerator(
    private val pluginContext: IrPluginContext,
    private val moduleFragment: IrModuleFragment,
    private val previewsListPackage: String,
    private val publicPreviewList: Boolean,
) {
    private val irFactory = pluginContext.irFactory
    private val irBuiltIns = pluginContext.irBuiltIns

    private val collectedPreviewClassId = ClassId(FqName("me.tbsten.compose.preview.lab"), Name.identifier("CollectedPreview"))
    private val collectedPreviewClass by lazy { pluginContext.referenceClass(collectedPreviewClassId)!! }
    private val collectedPreviewType by lazy { collectedPreviewClass.defaultType }
    private val collectedPreviewConstructor by lazy { collectedPreviewClass.owner.constructors.first() }

    private val listClassId = ClassId(FqName("kotlin.collections"), Name.identifier("List"))
    private val listClass by lazy { pluginContext.referenceClass(listClassId)!! }
    private val listOfCollectedPreviewType by lazy { listClass.typeWith(collectedPreviewType) }

    fun generate(previews: List<PreviewMetadata>) {
        val sortedPreviews = previews.sortedBy { it.displayName }

        val fileEntry = createSyntheticFileEntry("PreviewList.kt")

        val irFile = irFactory.createFile(
            fileEntry = fileEntry,
            packageFqName = FqName(previewsListPackage),
            module = moduleFragment.descriptor,
        )

        val previewListObject = irFactory.buildClass {
            name = Name.identifier("PreviewList")
            kind = ClassKind.OBJECT
            modality = Modality.FINAL
            visibility = if (publicPreviewList) DescriptorVisibilities.PUBLIC else DescriptorVisibilities.INTERNAL
        }.apply {
            parent = irFile
            superTypes = listOf(listOfCollectedPreviewType)
            createImplicitParameterDeclarationWithWrappedDescriptor()

            addConstructor {
                isPrimary = true
                visibility = DescriptorVisibilities.PRIVATE
            }.apply {
                body = irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET).apply {
                    statements.add(
                        IrDelegatingConstructorCallImpl.fromSymbolOwner(
                            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                            irBuiltIns.anyType,
                            irBuiltIns.anyClass.owner.constructors.first().symbol,
                        ),
                    )
                    statements.add(
                        IrInstanceInitializerCallImpl(
                            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                            this@apply.parent.let { (it as org.jetbrains.kotlin.ir.declarations.IrClass).symbol },
                            irBuiltIns.unitType,
                        ),
                    )
                }
            }

            addField {
                this.name = Name.identifier("\$\$delegate_0")
                type = listOfCollectedPreviewType
                visibility = DescriptorVisibilities.PRIVATE
                isFinal = true
            }.apply {
                initializer = irFactory.createExpressionBody(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                    buildDelegateExpression(sortedPreviews),
                )
            }
        }

        irFile.declarations.add(previewListObject)
        moduleFragment.files.add(irFile)
    }

    private fun buildDelegateExpression(
        sortedPreviews: List<PreviewMetadata>,
    ): org.jetbrains.kotlin.ir.expressions.IrExpression {
        val listOfFunction = pluginContext.referenceFunctions(
            CallableId(FqName("kotlin.collections"), Name.identifier("listOf")),
        ).first { it.owner.valueParameters.size == 1 && it.owner.valueParameters[0].isVararg }

        val collectedPreviewEntries = sortedPreviews.map { preview ->
            buildCollectedPreviewCall(preview)
        }

        val vararg = IrVarargImpl(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
            type = irBuiltIns.arrayClass.typeWith(collectedPreviewType),
            varargElementType = collectedPreviewType,
        ).apply {
            elements.addAll(collectedPreviewEntries)
        }

        val listOfCall = IrCallImpl(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
            type = listOfCollectedPreviewType,
            symbol = listOfFunction,
            typeArgumentsCount = 1,
            valueArgumentsCount = 1,
        ).apply {
            putTypeArgument(0, collectedPreviewType)
            putValueArgument(0, vararg)
        }

        val sortedByFunction = pluginContext.referenceFunctions(
            CallableId(FqName("kotlin.collections"), Name.identifier("sortedBy")),
        ).firstOrNull { func ->
            func.owner.extensionReceiverParameter != null && func.owner.valueParameters.size == 1
        }

        return if (sortedByFunction != null) {
            val sortedByLambda = buildSortedByLambda()

            IrCallImpl(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                type = listOfCollectedPreviewType,
                symbol = sortedByFunction,
                typeArgumentsCount = 2,
                valueArgumentsCount = 1,
            ).apply {
                extensionReceiver = listOfCall
                putTypeArgument(0, collectedPreviewType)
                putTypeArgument(1, irBuiltIns.stringType)
                putValueArgument(0, sortedByLambda)
            }
        } else {
            listOfCall
        }
    }

    private fun buildCollectedPreviewCall(preview: PreviewMetadata): org.jetbrains.kotlin.ir.expressions.IrExpression {
        val constructor = collectedPreviewConstructor.symbol

        val constructorCall = IrConstructorCallImpl(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
            type = collectedPreviewType,
            symbol = constructor,
            typeArgumentsCount = 0,
            constructorTypeArgumentsCount = 0,
            valueArgumentsCount = constructor.owner.valueParameters.size,
        )

        for ((index, param) in constructor.owner.valueParameters.withIndex()) {
            val value: org.jetbrains.kotlin.ir.expressions.IrExpression? = when (param.name.asString()) {
                "id" -> irString(preview.id)
                "displayName" -> irString(preview.displayName)
                "filePath" -> preview.filePath?.let { irString(it) } ?: irNull(irBuiltIns.stringType.makeNullable())
                "startLineNumber" -> preview.startLineNumber?.let { irInt(it) } ?: irNull(irBuiltIns.intType.makeNullable())
                "code" -> preview.code?.let { irString(it) } ?: irNull(irBuiltIns.stringType.makeNullable())
                "kdoc" -> preview.kdoc?.let { irString(it) } ?: irNull(irBuiltIns.stringType.makeNullable())
                "content" -> buildContentLambda(preview)
                else -> null
            }
            if (value != null) {
                constructorCall.putValueArgument(index, value)
            }
        }

        return constructorCall
    }

    private fun buildContentLambda(preview: PreviewMetadata): org.jetbrains.kotlin.ir.expressions.IrExpression {
        val composableAnnotation = pluginContext.referenceClass(
            ClassId(FqName("androidx.compose.runtime"), Name.identifier("Composable")),
        )

        val lambdaFunction = irFactory.buildFun {
            name = SpecialNames.ANONYMOUS
            returnType = irBuiltIns.unitType
            visibility = DescriptorVisibilities.LOCAL
            origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
        }.apply {
            if (composableAnnotation != null) {
                annotations = listOf(
                    IrConstructorCallImpl(
                        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                        type = composableAnnotation.defaultType,
                        symbol = composableAnnotation.owner.constructors.first().symbol,
                        typeArgumentsCount = 0,
                        constructorTypeArgumentsCount = 0,
                        valueArgumentsCount = 0,
                    ),
                )
            }

            body = irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET).apply {
                statements.add(
                    IrCallImpl(
                        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                        type = irBuiltIns.unitType,
                        symbol = preview.irFunction.symbol,
                        typeArgumentsCount = 0,
                        valueArgumentsCount = 0,
                    ),
                )
            }
        }

        val functionType = irBuiltIns.functionN(0).typeWith(irBuiltIns.unitType)

        return IrFunctionExpressionImpl(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
            type = functionType,
            function = lambdaFunction,
            origin = IrStatementOrigin.LAMBDA,
        )
    }

    private fun buildSortedByLambda(): org.jetbrains.kotlin.ir.expressions.IrExpression {
        val lambdaFunction = irFactory.buildFun {
            name = SpecialNames.ANONYMOUS
            returnType = irBuiltIns.stringType
            visibility = DescriptorVisibilities.LOCAL
            origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
        }.apply {
            val itParam = addValueParameter("it", collectedPreviewType)

            val displayNameGetter = collectedPreviewClass.owner.declarations
                .filterIsInstance<IrProperty>()
                .firstOrNull { it.name.asString() == "displayName" }
                ?.getter

            body = irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET).apply {
                if (displayNameGetter != null) {
                    val getDisplayName = IrCallImpl(
                        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                        type = irBuiltIns.stringType,
                        symbol = displayNameGetter.symbol,
                        typeArgumentsCount = 0,
                        valueArgumentsCount = 0,
                    ).apply {
                        dispatchReceiver = IrGetValueImpl(
                            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                            itParam.type,
                            itParam.symbol,
                        )
                    }
                    statements.add(
                        IrReturnImpl(
                            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                            irBuiltIns.nothingType,
                            this@apply.symbol,
                            getDisplayName,
                        ),
                    )
                }
            }
        }

        val functionType = irBuiltIns.functionN(1).typeWith(collectedPreviewType, irBuiltIns.stringType)

        return IrFunctionExpressionImpl(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
            type = functionType,
            function = lambdaFunction,
            origin = IrStatementOrigin.LAMBDA,
        )
    }

    private fun irString(value: String) = IrConstImpl.string(
        UNDEFINED_OFFSET, UNDEFINED_OFFSET, irBuiltIns.stringType, value,
    )

    private fun irInt(value: Int) = IrConstImpl.int(
        UNDEFINED_OFFSET, UNDEFINED_OFFSET, irBuiltIns.intType, value,
    )

    private fun irNull(type: IrType) = IrConstImpl.constNull(
        UNDEFINED_OFFSET, UNDEFINED_OFFSET, type,
    )
}

internal fun createSyntheticFileEntry(name: String) = object : org.jetbrains.kotlin.ir.IrFileEntry {
    override val name: String = name
    override val maxOffset: Int = UNDEFINED_OFFSET
    override fun getLineNumber(offset: Int): Int = UNDEFINED_OFFSET
    override fun getColumnNumber(offset: Int): Int = UNDEFINED_OFFSET
    override fun getSourceRangeInfo(beginOffset: Int, endOffset: Int) =
        org.jetbrains.kotlin.ir.SourceRangeInfo(name, beginOffset, -1, -1, endOffset, -1, -1)
}
