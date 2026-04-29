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
import org.jetbrains.kotlin.platform.jvm.isJvm

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
    private val moduleFragment: org.jetbrains.kotlin.ir.declarations.IrModuleFragment,
    previews: List<PreviewFunctionInfo>,
    private val compatContext: CompatContext,
) : IrElementTransformerVoid() {

    private val collectModulePreviewsFq = FqName("me.tbsten.compose.preview.lab.collectModulePreviews")
    private val collectAllModulePreviewsFq = FqName("me.tbsten.compose.preview.lab.collectAllModulePreviews")

    private val irBuilder = PreviewListIrBuilder(pluginContext, previews, config, compatContext)

    private val generateHint by lazy {
        GeneratePreviewExportHint(pluginContext, moduleFragment, compatContext)
    }

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
     * Replaces the placeholder initializer of a preview collection property with the actual IR logic to aggregate and export `@Preview` functions.
     *
     * This method transforms a property's backing field (typically a delegate) from a simple sentinel call into a
     * complete implementation that builds the preview list, wraps it in a [lazy] block, and exports it for
     * cross-module discovery.
     *
     * # Transformation Logic
     * The original call to `collectModulePreviews()` or `collectAllModulePreviews()` is replaced with:
     * 1. **List Construction**: Generates IR to create a `List<CollectedPreview>` containing all discovered previews.
     * 2. **Dependency Aggregation**: If `collectAllModulePreviews()` is used, it also includes previews exported by upstream modules.
     * 3. **Lazy Wrapping**: The list is wrapped in `kotlin.lazy` to ensure initialization only occurs when accessed.
     * 4. **Export Metadata**: Wraps the result in a `PreviewExport` container.
     *
     * # JVM Hint Generation
     * On JVM platforms, this method also triggers the generation of a "hint" function. This hint allows
     * downstream modules to discover this property as a source of previews through IR scanning, enabling
     * seamless multi-module preview aggregation.
     *
     * # Example
     * ## Source Code:
     * ```kt
     * val myPreviews by collectModulePreviews()
     * ```
     *
     * ## Transformed IR (Conceptual):
     * ```kt
     * val myPreviews$delegate = PreviewExport(lazy {
     *     listOf(
     *         CollectedPreview(id = "Preview1", ...),
     *         CollectedPreview(id = "Preview2", ...),
     *     )
     * })
     * ```
     *
     * @param property The [IrProperty] declaration whose initializer needs to be replaced.
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
        val previewExportExpr = irBuilder.buildPreviewExportCall(builder, lazyExpr)

        delegateField.initializer = pluginContext.irFactory.createExpressionBody(
            startOffset = property.startOffset,
            endOffset = property.endOffset,
            expression = previewExportExpr,
        )

        // Generate a hint function so downstream `collectAllModulePreviews()` can discover
        // this property without any Gradle configuration.
        //
        // **JVM-only**: all hints share the same `(previewLabExport, PreviewExport)` IR signature.
        // On JVM the file-facade class disambiguates them at the bytecode level. KLIB-based
        // platforms (JS / Wasm / iOS) compute IdSignature from `(name, parameter types)` only and
        // would treat hints from different modules as duplicates at link time. Skipping emission
        // on non-JVM keeps the build green; cross-module aggregation falls back to single-module
        // there. See `GeneratePreviewExportHint` KDoc for the full rationale.
        //
        // We emit hints for both `collectModulePreviews()` and `collectAllModulePreviews()`
        // properties: the latter is needed so a downstream `collectAllModulePreviews()` can pick
        // up an intermediate module that has already aggregated its own dependencies (e.g.
        // `app(all) → ui(all) → core(single)` should yield app + ui + core previews).
        // Duplicates that arise from overlapping hint chains are filtered out in
        // `PreviewListIrBuilder.buildConcatenatedPreviewsExpr` via `distinctPreviewsById`.
        val sourceFile = property.parent as? org.jetbrains.kotlin.ir.declarations.IrFile
        if (property.isDelegated && sourceFile != null && pluginContext.platform?.isJvm() == true) {
            val pkg = sourceFile.packageFqName.asString()
            val propertyName = property.name.asString()
            val fqn = if (pkg.isEmpty()) propertyName else "$pkg.$propertyName"
            generateHint(fqn, sourceFile)
        }
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
