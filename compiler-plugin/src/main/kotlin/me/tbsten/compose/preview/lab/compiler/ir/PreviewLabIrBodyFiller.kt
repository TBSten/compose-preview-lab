@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.ir

import me.tbsten.compose.preview.lab.compiler.PluginConfig
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrClass
import me.tbsten.compose.preview.lab.compiler.compat.IrDeclarationOriginCompat
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * Transforms the IR tree to replace preview-collection property initializers
 * with the actual list of collected `@Preview` functions.
 *
 * Handles four patterns:
 * 1. `val x by collectModulePreviews()` — this module's previews only
 * 2. `val x by collectAllModulePreviews()` — this module + dependency modules
 * 3. (Legacy) `object PreviewList : List<CollectedPreview> by emptyList()` — delegate field replacement
 * 4. (Legacy) aggregate property — getter replacement
 */
internal class PreviewLabIrBodyFiller(
    private val pluginContext: IrPluginContext,
    private val config: PluginConfig,
    previews: List<PreviewFunctionInfo>,
) : IrElementTransformerVoid() {

    private val collectModulePreviewsFq = FqName("me.tbsten.compose.preview.lab.collectModulePreviews")
    private val collectAllModulePreviewsFq = FqName("me.tbsten.compose.preview.lab.collectAllModulePreviews")

    private val previewsListPackage = FqName(config.previewsListPackage)
    private val aggregatePropertyPackage = FqName("me.tbsten.compose.preview.lab.generated")
    private val previewListFqName = previewsListPackage.child(Name.identifier("PreviewList"))
    private val previewAllListFqName = previewsListPackage.child(Name.identifier("PreviewAllList"))

    private val irBuilder = PreviewListIrBuilder(pluginContext, previews, config)

    // ----- collectModulePreviews / collectAllModulePreviews -----

    /**
     * Visits each property declaration and replaces the initializer if it matches
     * `collectModulePreviews()`, `collectAllModulePreviews()`, or the legacy aggregate pattern.
     */
    override fun visitProperty(declaration: IrProperty): IrStatement {
        if (isCollectPreviewsCall(declaration)) {
            replaceCollectPreviewsProperty(declaration)
            return super.visitProperty(declaration)
        }

        if (isAggregateProperty(declaration)) {
            replaceAggregatePropertyInitializer(declaration)
            return super.visitProperty(declaration)
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

        // 生成する lambda の parent は IrFunction でなければならない。
        // property の delegate field initializer は static initializer (`<clinit>`) で実行されるが、
        // その IrFunction はまだこの phase で存在しないため、property の getter を仮の親として使う。
        // (Kotlin 2.3 以降の JVM backend は parent が IrFile の lambda で
        // `MethodSignatureMapper.mapToMethodHandle` の "Unexpected parent: FILE" assert を投げる)
        val lambdaParent: IrDeclarationParent = property.getter ?: property.parent

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

    // ----- Legacy: object PreviewList -----

    /**
     * Visits object declarations and replaces the delegate field initializer
     * for `PreviewList` / `PreviewAllList` objects generated by the legacy source-generation approach.
     */
    override fun visitClass(declaration: IrClass): IrStatement {
        if (declaration.kind != ClassKind.OBJECT) return super.visitClass(declaration)
        val fqName = declaration.kotlinFqName
        if (fqName != previewListFqName && fqName != previewAllListFqName) {
            return super.visitClass(declaration)
        }
        replaceDelegateInitializer(declaration)
        return super.visitClass(declaration)
    }

    /**
     * Replaces the `$$delegate_0` field initializer of a legacy
     * `object PreviewList : List<CollectedPreview> by emptyList()` with the actual preview list.
     */
    private fun replaceDelegateInitializer(objectClass: IrClass) {
        val delegateField = objectClass.declarations
            .filterIsInstance<IrField>()
            .firstOrNull { it.origin == IrDeclarationOriginCompat.DELEGATE }
            ?: return

        val builder = DeclarationIrBuilder(pluginContext, objectClass.thisReceiver!!.symbol)
        delegateField.initializer = pluginContext.irFactory.createExpressionBody(
            startOffset = objectClass.startOffset,
            endOffset = objectClass.endOffset,
            expression = irBuilder.buildPreviewsListExpr(builder, objectClass),
        )
    }

    // ----- Legacy: aggregate property -----

    /**
     * Checks whether the property matches the legacy aggregate property pattern
     * (`__<package>__previewsForAggregateAll` in `me.tbsten.compose.preview.lab.generated`).
     */
    private fun isAggregateProperty(declaration: IrProperty): Boolean {
        val expectedName = "__${config.previewsListPackage.replace('.', '_')}__previewsForAggregateAll"
        if (declaration.name.asString() != expectedName) return false
        val parentPkg = when (val parent = declaration.parent) {
            is org.jetbrains.kotlin.ir.declarations.IrPackageFragment -> parent.packageFqName
            is org.jetbrains.kotlin.ir.declarations.IrFile -> parent.packageFqName
            else -> return false
        }
        return parentPkg == aggregatePropertyPackage
    }

    /**
     * Replaces the getter of a legacy aggregate property with the actual preview list expression.
     * Removes the backing field so no stale `GET_FIELD` references remain.
     */
    private fun replaceAggregatePropertyInitializer(property: IrProperty) {
        val getter = property.getter ?: return
        property.backingField = null
        val builder = DeclarationIrBuilder(pluginContext, getter.symbol)
        getter.body = builder.irBlockBody {
            +irReturn(irBuilder.buildPreviewsListExpr(builder, getter))
        }
    }
}
