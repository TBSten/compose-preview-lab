@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.ir

import me.tbsten.compose.preview.lab.compiler.PluginConfig
import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.FqName

/**
 * Transforms the IR tree to replace preview-collection property initializers
 * with the actual list of collected `@Preview` functions.
 *
 * Handles two patterns:
 * 1. `val x by collectModulePreviews()` — this module's previews only
 * 2. `val x by collectAllModulePreviews()` — this module + dependency modules
 */
internal class PreviewLabIrBodyFiller(
    private val pluginContext: IrPluginContext,
    private val config: PluginConfig,
    previews: List<PreviewFunctionInfo>,
    compatContext: CompatContext,
) : IrElementTransformerVoid() {

    private val collectModulePreviewsFq = FqName("me.tbsten.compose.preview.lab.collectModulePreviews")
    private val collectAllModulePreviewsFq = FqName("me.tbsten.compose.preview.lab.collectAllModulePreviews")

    private val irBuilder = PreviewListIrBuilder(pluginContext, previews, config, compatContext)

    /**
     * Visits each property declaration and replaces the initializer if it matches
     * `collectModulePreviews()` or `collectAllModulePreviews()`.
     */
    override fun visitProperty(declaration: IrProperty): IrStatement {
        if (isCollectPreviewsCall(declaration)) {
            replaceCollectPreviewsProperty(declaration)
        }
        return super.visitProperty(declaration)
    }

    /**
     * Checks whether the property's backing field is initialized with a call to
     * `collectModulePreviews()` or `collectAllModulePreviews()`.
     *
     * These are the sentinel functions that signal the compiler plugin to inject
     * the collected preview list.
     */
    private fun isCollectPreviewsCall(property: IrProperty): Boolean {
        val init = property.backingField?.initializer?.expression
        if (init !is org.jetbrains.kotlin.ir.expressions.IrCall) return false
        val fqn = init.symbol.owner.kotlinFqName
        return fqn == collectModulePreviewsFq || fqn == collectAllModulePreviewsFq
    }

    /**
     * Replaces the `Lazy` delegate field of a `collectModulePreviews()` / `collectAllModulePreviews()`
     * property with a `lazy { listOf(CollectedPreview(...), ...) }` expression.
     *
     * For `collectAllModulePreviews()`, the generated list also includes previews from
     * dependency modules resolved via [PluginConfig.dependencyCollectPreviewsFqns].
     */
    private fun replaceCollectPreviewsProperty(property: IrProperty) {
        val delegateField = property.backingField ?: return
        val isAll = isCollectAllCall(delegateField)
        val builder = DeclarationIrBuilder(pluginContext, property.symbol)

        // The synthetic lambda needs an IrFunction as its parent.
        // The delegate field initializer ultimately runs inside a static initializer (`<clinit>`),
        // but that IrFunction does not exist yet at this phase, so we use the property's getter
        // as a stand-in parent.
        // (The Kotlin 2.3+ JVM backend asserts on lambdas whose parent is an IrFile via
        // `MethodSignatureMapper.mapToMethodHandle` with "Unexpected parent: FILE".)
        val lambdaParent: IrDeclarationParent = property.getter
            ?: error(
                "collectModulePreviews/collectAllModulePreviews delegate must be on a property" +
                    " with a getter, not a backing field",
            )

        val thisModulePreviews = irBuilder.buildPreviewsListExpr(builder, lambdaParent)
        val previewListExpr = if (isAll) {
            irBuilder.buildConcatenatedPreviewsExpr(builder, thisModulePreviews)
        } else {
            thisModulePreviews
        }
        val lazyExpr = irBuilder.buildLazyCall(builder, previewListExpr, lambdaParent)

        delegateField.initializer = pluginContext.irFactory.createExpressionBody(
            startOffset = property.startOffset,
            endOffset = property.endOffset,
            expression = lazyExpr,
        )
    }

    /**
     * Checks whether the delegate field was initialized with `collectAllModulePreviews()`
     * (as opposed to `collectModulePreviews()`).
     */
    private fun isCollectAllCall(delegateField: IrField): Boolean {
        val initializer = delegateField.initializer?.expression ?: return false
        if (initializer !is org.jetbrains.kotlin.ir.expressions.IrCall) return false
        return initializer.symbol.owner.kotlinFqName == collectAllModulePreviewsFq
    }
}
