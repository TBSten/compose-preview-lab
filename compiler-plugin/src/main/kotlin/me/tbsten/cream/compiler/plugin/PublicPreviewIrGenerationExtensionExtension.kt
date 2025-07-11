package me.tbsten.cream.compiler.plugin

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable

class PublicPreviewIrGenerationExtensionExtension(private val annotations: List<String>) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        if (annotations.isEmpty()) {
            error("PublicPreviewPlugin is enabled but no annotations are specified.")
        }

        moduleFragment.transform(PublicPreviewIrVisitor(pluginContext, annotations), null)
    }
}

@OptIn(UnsafeDuringIrConstructionAPI::class)
class PublicPreviewIrVisitor(private val pluginContext: IrPluginContext, private val annotations: List<String>) :
    IrElementTransformerVoidWithContext() {
    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        val hasPreviewAnnotation =
            declaration.annotations.any {
                it.symbol.owner.fqNameWhenAvailable?.asString() in annotations
            }

        if (hasPreviewAnnotation) {
            println("PublicPreview: find ${declaration.name.asString()} ")
            declaration.visibility = DescriptorVisibilities.PUBLIC
        }

        return super.visitSimpleFunction(declaration)
    }
}
