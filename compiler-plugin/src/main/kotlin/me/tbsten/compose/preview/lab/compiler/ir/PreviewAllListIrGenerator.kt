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
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetObjectValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrReturnImpl
import org.jetbrains.kotlin.ir.expressions.impl.fromSymbolOwner
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.createImplicitParameterDeclarationWithWrappedDescriptor
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

internal class PreviewAllListIrGenerator(
    private val pluginContext: IrPluginContext,
    private val moduleFragment: IrModuleFragment,
    private val previewsListPackage: String,
    private val hasPreviews: Boolean,
) {
    private val irFactory = pluginContext.irFactory
    private val irBuiltIns = pluginContext.irBuiltIns

    private val collectedPreviewClassId = ClassId(FqName("me.tbsten.compose.preview.lab"), Name.identifier("CollectedPreview"))
    private val collectedPreviewClass by lazy { pluginContext.referenceClass(collectedPreviewClassId)!! }
    private val collectedPreviewType by lazy { collectedPreviewClass.defaultType }

    private val listClassId = ClassId(FqName("kotlin.collections"), Name.identifier("List"))
    private val listClass by lazy { pluginContext.referenceClass(listClassId)!! }
    private val listOfCollectedPreviewType by lazy { listClass.typeWith(collectedPreviewType) }

    private val aggregateToAllFqName = FqName("me.tbsten.compose.preview.lab.AggregateToAll")

    fun generate() {
        if (hasPreviews) {
            generateAggregateMarker()
        }
        generatePreviewAllList()
    }

    private fun generateAggregateMarker() {
        val fileEntry = createSyntheticFileEntry("__${previewsListPackage}__previewsForAggregateAll.kt")

        val irFile = irFactory.createFile(
            fileEntry = fileEntry,
            packageFqName = FqName("me.tbsten.compose.preview.lab.generated"),
            module = moduleFragment.descriptor,
        )

        val propertyName = "__${previewsListPackage}__previewsForAggregateAll"

        val property = irFactory.createProperty(
            startOffset = UNDEFINED_OFFSET,
            endOffset = UNDEFINED_OFFSET,
            origin = IrDeclarationOrigin.DEFINED,
            name = Name.identifier(propertyName),
            visibility = DescriptorVisibilities.PUBLIC,
            modality = Modality.FINAL,
            symbol = org.jetbrains.kotlin.ir.symbols.impl.IrPropertySymbolImpl(),
            isVar = false,
            isConst = false,
            isLateinit = false,
            isDelegated = false,
            isExternal = false,
            isExpect = false,
            isFakeOverride = false,
            containerSource = null,
        ).apply {
            parent = irFile

            val aggregateToAllClass = pluginContext.referenceClass(
                ClassId(FqName("me.tbsten.compose.preview.lab"), Name.identifier("AggregateToAll")),
            )
            val internalApiClass = pluginContext.referenceClass(
                ClassId(FqName("me.tbsten.compose.preview.lab"), Name.identifier("InternalComposePreviewLabApi")),
            )

            annotations = buildList {
                aggregateToAllClass?.let { cls ->
                    add(
                        IrConstructorCallImpl(
                            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                            type = cls.defaultType,
                            symbol = cls.owner.constructors.first().symbol,
                            typeArgumentsCount = 0,
                            constructorTypeArgumentsCount = 0,
                            valueArgumentsCount = 0,
                        ),
                    )
                }
                internalApiClass?.let { cls ->
                    add(
                        IrConstructorCallImpl(
                            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                            type = cls.defaultType,
                            symbol = cls.owner.constructors.first().symbol,
                            typeArgumentsCount = 0,
                            constructorTypeArgumentsCount = 0,
                            valueArgumentsCount = 0,
                        ),
                    )
                }
            }

            val previewListClass = findPreviewListInModule()

            backingField = irFactory.createField(
                startOffset = UNDEFINED_OFFSET,
                endOffset = UNDEFINED_OFFSET,
                origin = IrDeclarationOrigin.PROPERTY_BACKING_FIELD,
                name = Name.identifier(propertyName),
                visibility = DescriptorVisibilities.PRIVATE,
                symbol = org.jetbrains.kotlin.ir.symbols.impl.IrFieldSymbolImpl(),
                type = listOfCollectedPreviewType,
                isFinal = true,
                isStatic = true,
                isExternal = false,
            ).apply {
                parent = irFile
                if (previewListClass != null) {
                    initializer = irFactory.createExpressionBody(
                        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                        IrGetObjectValueImpl(
                            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                            listOfCollectedPreviewType,
                            previewListClass.symbol,
                        ),
                    )
                }
            }
        }

        irFile.declarations.add(property)
        moduleFragment.files.add(irFile)
    }

    private fun findPreviewListInModule(): org.jetbrains.kotlin.ir.declarations.IrClass? {
        for (file in moduleFragment.files) {
            if (file.packageFqName.asString() != previewsListPackage) continue
            for (declaration in file.declarations) {
                if (declaration is org.jetbrains.kotlin.ir.declarations.IrClass &&
                    declaration.name.asString() == "PreviewList"
                ) {
                    return declaration
                }
            }
        }
        return null
    }

    private fun generatePreviewAllList() {
        val fileEntry = createSyntheticFileEntry("PreviewAllList.kt")

        val irFile = irFactory.createFile(
            fileEntry = fileEntry,
            packageFqName = FqName(previewsListPackage),
            module = moduleFragment.descriptor,
        )

        val aggregateProperties = findAggregateProperties()

        val previewAllListObject = irFactory.buildClass {
            name = Name.identifier("PreviewAllList")
            kind = ClassKind.OBJECT
            modality = Modality.FINAL
            visibility = DescriptorVisibilities.PUBLIC
        }.apply {
            parent = irFile
            superTypes = listOf(listOfCollectedPreviewType)
            createImplicitParameterDeclarationWithWrappedDescriptor()

            val experimentalApiClass = pluginContext.referenceClass(
                ClassId(FqName("me.tbsten.compose.preview.lab"), Name.identifier("ExperimentalComposePreviewLabApi")),
            )
            if (experimentalApiClass != null) {
                annotations = listOf(
                    IrConstructorCallImpl(
                        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                        type = experimentalApiClass.defaultType,
                        symbol = experimentalApiClass.owner.constructors.first().symbol,
                        typeArgumentsCount = 0,
                        constructorTypeArgumentsCount = 0,
                        valueArgumentsCount = 0,
                    ),
                )
            }

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
                    buildPreviewAllDelegateExpression(aggregateProperties),
                )
            }
        }

        irFile.declarations.add(previewAllListObject)
        moduleFragment.files.add(irFile)
    }

    private fun findAggregateProperties(): List<org.jetbrains.kotlin.ir.symbols.IrPropertySymbol> =
        try {
            pluginContext.referenceProperties(
                CallableId(FqName("me.tbsten.compose.preview.lab.generated"), Name.special("<all>")),
            ).filter { prop ->
                prop.owner.annotations.any { it.type.classFqName == aggregateToAllFqName }
            }.toList()
        } catch (_: Exception) {
            val results = mutableListOf<org.jetbrains.kotlin.ir.symbols.IrPropertySymbol>()
            for (file in moduleFragment.files) {
                if (file.packageFqName.asString() != "me.tbsten.compose.preview.lab.generated") continue
                for (declaration in file.declarations) {
                    if (declaration is IrProperty && declaration.hasAnnotation(aggregateToAllFqName)) {
                        results.add(declaration.symbol)
                    }
                }
            }
            results
        }

    private fun buildPreviewAllDelegateExpression(
        aggregateProperties: List<org.jetbrains.kotlin.ir.symbols.IrPropertySymbol>,
    ): org.jetbrains.kotlin.ir.expressions.IrExpression {
        val previewListClass = findPreviewListInModule()
        var expression: org.jetbrains.kotlin.ir.expressions.IrExpression = if (previewListClass != null) {
            IrGetObjectValueImpl(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                listOfCollectedPreviewType,
                previewListClass.symbol,
            )
        } else {
            val emptyListFn = pluginContext.referenceFunctions(
                CallableId(FqName("kotlin.collections"), Name.identifier("emptyList")),
            ).first()
            IrCallImpl(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                type = listOfCollectedPreviewType,
                symbol = emptyListFn,
                typeArgumentsCount = 1,
                valueArgumentsCount = 0,
            ).apply {
                putTypeArgument(0, collectedPreviewType)
            }
        }

        val plusFunction = pluginContext.referenceFunctions(
            CallableId(FqName("kotlin.collections"), Name.identifier("plus")),
        ).firstOrNull { func ->
            func.owner.extensionReceiverParameter != null &&
                func.owner.valueParameters.size == 1 &&
                func.owner.extensionReceiverParameter?.type?.classFqName?.asString() == "kotlin.collections.Collection"
        }

        if (plusFunction != null) {
            for (prop in aggregateProperties) {
                val getter = prop.owner.getter ?: continue
                val getPropertyCall = IrCallImpl(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                    type = listOfCollectedPreviewType,
                    symbol = getter.symbol,
                    typeArgumentsCount = 0,
                    valueArgumentsCount = 0,
                )

                expression = IrCallImpl(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                    type = listOfCollectedPreviewType,
                    symbol = plusFunction,
                    typeArgumentsCount = 1,
                    valueArgumentsCount = 1,
                ).apply {
                    extensionReceiver = expression
                    putTypeArgument(0, collectedPreviewType)
                    putValueArgument(0, getPropertyCall)
                }
            }
        }

        val distinctByFunction = pluginContext.referenceFunctions(
            CallableId(FqName("kotlin.collections"), Name.identifier("distinctBy")),
        ).firstOrNull()

        return if (distinctByFunction != null) {
            val distinctByLambda = buildDistinctByIdLambda()

            IrCallImpl(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                type = listOfCollectedPreviewType,
                symbol = distinctByFunction,
                typeArgumentsCount = 2,
                valueArgumentsCount = 1,
            ).apply {
                extensionReceiver = expression
                putTypeArgument(0, collectedPreviewType)
                putTypeArgument(1, irBuiltIns.stringType)
                putValueArgument(0, distinctByLambda)
            }
        } else {
            expression
        }
    }

    private fun buildDistinctByIdLambda(): org.jetbrains.kotlin.ir.expressions.IrExpression {
        val lambdaFunction = irFactory.buildFun {
            name = SpecialNames.ANONYMOUS
            returnType = irBuiltIns.stringType
            visibility = DescriptorVisibilities.LOCAL
            origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
        }.apply {
            val itParam = addValueParameter("it", collectedPreviewType)

            val idGetter = collectedPreviewClass.owner.declarations
                .filterIsInstance<IrProperty>()
                .firstOrNull { it.name.asString() == "id" }
                ?.getter

            body = irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET).apply {
                if (idGetter != null) {
                    val getIdCall = IrCallImpl(
                        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                        type = irBuiltIns.stringType,
                        symbol = idGetter.symbol,
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
                            getIdCall,
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
}
