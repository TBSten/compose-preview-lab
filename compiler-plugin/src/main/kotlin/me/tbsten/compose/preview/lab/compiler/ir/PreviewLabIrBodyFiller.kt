@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.ir

import me.tbsten.compose.preview.lab.compiler.PluginConfig
import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.file
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
    private val moduleFragment: org.jetbrains.kotlin.ir.declarations.IrModuleFragment,
    previews: List<PreviewFunctionInfo>,
    private val compatContext: CompatContext,
    private val messageCollector: MessageCollector = MessageCollector.NONE,
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
     * Replaces the placeholder initializer of a preview collection property with the actual IR.
     *
     * **Before** (`collectModulePreviews()`):
     * ```kotlin
     * val myPreviews by collectModulePreviews()
     * // delegate field initializer = collectModulePreviews() sentinel call
     * ```
     *
     * **After** (semantically equivalent):
     * ```kotlin
     * val myPreviews by PreviewExport(
     *     lazy {
     *         listOf(
     *             CollectedPreview(
     *                 id = "com.example.MyButton",
     *                 displayName = "com.example.MyButton",
     *                 filePath = "src/main/kotlin/com/example/MyButton.kt",
     *                 startLineNumber = 10,
     *                 endLineNumber = 15,
     *                 code = "{ ... }",
     *                 kdoc = null,
     *             ) { MyButton() },
     *         )
     *     }
     * )
     * ```
     *
     * For `collectAllModulePreviews()`, dependency previews are appended and deduplicated:
     * ```kotlin
     * val appPreviews by PreviewExport(
     *     lazy {
     *         distinctPreviewsById(
     *             mutableListOf<CollectedPreview>().apply {
     *                 addAll(listOf(CollectedPreview(...) { MyButton() })) // this module
     *                 add(previewHint(null))                                // each @Preview from a dep module via per-declaration hint
     *                 add(previewHint(null))
     *             }
     *         )
     *     }
     * )
     * ```
     *
     * Cross-module discovery is implemented by [HintDiscovery] using `referenceFunctions`
     * to find the `previewHint(value: PreviewHintMarker_<hash>?): CollectedPreview`
     * functions emitted by the per-declaration hint generator
     * ([me.tbsten.compose.preview.lab.compiler.fir.PreviewHintFirGenerator]).
     */
    private fun replaceCollectPreviewsProperty(property: IrProperty) {
        val delegateField = property.backingField ?: return
        val isAll = isCollectAllCall(delegateField)

        // Module-level gate: when `collectPreviewsEnabled = false` for this module the FIR
        // hint generator was not registered, so neither this module's previews nor any
        // dependency's hints can be discovered. Reporting an error keeps users from getting
        // a silently-empty list at runtime — flipping the flag is almost always a mistake
        // when paired with a `collect[All]ModulePreviews()` call site.
        if (!config.collectPreviewsEnabled) {
            reportCollectPreviewsDisabledError(property, isAll)
            return
        }

        // Version gate: cross-module aggregation requires Kotlin 2.3.21+ across every platform
        // (the FIR-based KLIB-safe hint generator depends on KT-82395 being fixed). When the
        // current compiler is older we report a structured error here so users get a clear
        // upgrade path rather than a silent half-broken aggregation.
        if (isAll && !compatContext.supportsKlibCrossModuleHint()) {
            reportUnsupportedCollectAllError(property)
            return
        }

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

        // **Kotlin 2.3.21+ only**: `PreviewLabHintFirGenerator` emits a per-module hint that points
        // at the auto-provider function or at the property FQN for manual sentinel properties.
        // Cross-module aggregation requires Kotlin 2.3.21+ for the FIR-based marker class
        // solution that avoids KLIB IdSignature collisions. Older Kotlin versions are no longer
        // supported by this compiler plugin.
    }

    /**
     * Reports the Kotlin-version-gate error for `collectAllModulePreviews()`.
     *
     * **Input** (semantically): the property's IR node for
     * ```kotlin
     * val previews by collectAllModulePreviews()
     * ```
     * compiled by a Kotlin compiler older than 2.3.21.
     *
     * **Output**: a `CompilerMessageSeverity.ERROR` reported through [messageCollector]
     * pointing at the property declaration, which causes the build to fail with a clear
     * upgrade-or-downgrade message. The IR is left untouched (the property keeps its
     * sentinel `collectAllModulePreviews()` initializer and the property's getter will
     * throw at runtime if the build somehow proceeds).
     */
    private fun reportUnsupportedCollectAllError(property: IrProperty) {
        messageCollector.report(
            CompilerMessageSeverity.ERROR,
            "[ComposePreviewLab] collectAllModulePreviews() requires Kotlin 2.3.21 or later " +
                "for cross-module preview aggregation. " +
                "Either upgrade Kotlin to 2.3.21+, or use collectModulePreviews() for " +
                "single-module collection.",
            propertyLocation(property),
        )
    }

    /**
     * Reports the disabled-module error for any `collect[All]ModulePreviews()` call site
     * found while `collectPreviewsEnabled = false`.
     *
     * **Input** (semantically): the property's IR node for
     * ```kotlin
     * val previews by collectModulePreviews()       // or collectAllModulePreviews()
     * ```
     * compiled with `composePreviewLab.collectPreviews.enabled = false` in the Gradle
     * configuration.
     *
     * **Output**: `CompilerMessageSeverity.ERROR` pointing at the property declaration.
     * The disabled flag suppresses every per-declaration hint emission for this module
     * (and consequently every cross-module aggregation it might participate in), so any
     * call site is almost certainly a configuration mistake — surfacing it as a compile
     * error is better than letting users observe a silently-empty list at runtime.
     */
    private fun reportCollectPreviewsDisabledError(property: IrProperty, isAll: Boolean) {
        val callName = if (isAll) "collectAllModulePreviews()" else "collectModulePreviews()"
        messageCollector.report(
            CompilerMessageSeverity.ERROR,
            "[ComposePreviewLab] $callName cannot be used in this module because " +
                "`composePreviewLab.collectPreviews.enabled` is false. " +
                "Either remove the call, or set `enabled = true` in the module's " +
                "composePreviewLab configuration.",
            propertyLocation(property),
        )
    }

    /** Resolves the source location of [property] for use in `MessageCollector` reports. */
    private fun propertyLocation(property: IrProperty): CompilerMessageLocation? {
        val entry = runCatching { property.file }.getOrNull()?.fileEntry ?: return null
        val offset = property.startOffset.takeIf { it >= 0 } ?: return null
        val line = entry.getLineNumber(offset) + 1
        val column = entry.getColumnNumber(offset) + 1
        return CompilerMessageLocation.create(entry.name, line, column, null)
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
