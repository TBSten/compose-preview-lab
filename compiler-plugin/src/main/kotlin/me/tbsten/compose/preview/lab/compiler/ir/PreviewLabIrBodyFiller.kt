@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.ir

import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.compiler.PluginConfig
import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.fir.PreviewLabFirBuiltIns
import me.tbsten.compose.preview.lab.compiler.ir.util.declarationLocation
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression
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
        val collectCall = delegateField.initializer?.expression as? IrCall ?: return
        val isAll = collectCall.symbol.owner.kotlinFqName == collectAllModulePreviewsFq

        // Module-level gate: when `collectPreviewsEnabled = false` for this module the FIR
        // hint generator was not registered, so no marker interface or `previewHint(...)`
        // overload was emitted alongside the @Preview functions. The @Preview functions
        // themselves are still compiled and live on the classpath; what is missing is the
        // discovery surface that `collect[All]ModulePreviews()` relies on. Dependency hints
        // (those imported from other modules) are still technically reachable, but we
        // intentionally treat the disabled module as "owns no preview surface" — pairing
        // the flag with a local collect call is almost always a configuration mistake.
        // Reporting an error keeps users from getting a silently-empty list at runtime.
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

        val callName = if (isAll) "collectAllModulePreviews" else "collectModulePreviews"
        // Regex validation runs in the FIR `CHECKERS` phase
        // (`me.tbsten.compose.preview.lab.compiler.fir.checkers.CollectScopeCallChecker`)
        // for inline string literals, but the FIR side cannot distinguish a `const val`
        // reference from a plain `val` reference at analysis time — they both arrive as
        // a `FirPropertyAccessExpression`. The const-folding into `IrConst<String>` only
        // happens between FIR and IR, so the IR pass is the first place we observe the
        // resolved-but-still-pre-checker `const val` value. We re-validate the regex here
        // so a `private const val BAD = "has space"; collectModulePreviews(scope = BAD)`
        // bypass would have been caught during embedding into the synthetic identifier.
        val scope = when (val resolution = resolveScopeArg(collectCall)) {
            is ScopeArgResult.Default -> config.defaultCollectScope
            is ScopeArgResult.Literal -> {
                val rawValue = resolution.value
                if (!PreviewLabFirBuiltIns.SCOPE_VALIDATION_REGEX.matches(rawValue)) {
                    reportInvalidScopeError(property, callName, rawValue)
                    return
                }
                // Sentinel substitution: a literal `"default"` (the
                // `ComposePreviewLabOption.DefaultCollectScope` value) means "use the
                // module's configured default scope" rather than the literal string.
                // This is what makes a Gradle-DSL-only library configuration work without
                // every consumer call having to repeat the scope string.
                if (rawValue == ComposePreviewLabOption.DefaultCollectScope) config.defaultCollectScope else rawValue
            }
            is ScopeArgResult.NonLiteral -> {
                reportNonLiteralScopeError(property, callName)
                return
            }
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

        val sequenceExpr = if (isAll) {
            irBuilder.buildConcatenatedPreviewsExpr(builder, lambdaParent, scope)
        } else {
            irBuilder.buildPreviewsSequenceExpr(builder, lambdaParent, scope)
        }
        val lazyExpr = irBuilder.buildLazyCall(builder, sequenceExpr, lambdaParent)
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
            declarationLocation(property),
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
                "the `collectPreviewsEnabled` plugin option is false " +
                "(typically set via `composePreviewLab.collectPreviews.enabled` in the Gradle DSL, " +
                "or via the `-P plugin:...:collectPreviewsEnabled=...` compiler option in non-Gradle setups). " +
                "Either remove the call, or re-enable the option for this module.",
            declarationLocation(property),
        )
    }

    /**
     * Reports the strict-literal-only error for `collect[All]ModulePreviews(scope = ...)`.
     *
     * The FIR `CollectScopeCallChecker` already flags clear-cut non-literals (string
     * concatenations, function calls). It cannot flag plain (non-`const`) `val` references
     * at FIR analysis time because `const val` references — which ARE allowed — look
     * identical to it (both arrive as `FirPropertyAccessExpression`). The IR pass is the
     * first place where the const-folded `IrConst<String>` distinction is observable, so
     * we keep this rejection here as the second-line check.
     */
    private fun reportNonLiteralScopeError(property: IrProperty, callName: String) {
        messageCollector.report(
            CompilerMessageSeverity.ERROR,
            "[ComposePreviewLab] $callName(scope = ...) accepts only a compile-time string " +
                "constant — an inline string literal or a `const val` reference. Non-`const` " +
                "vals, string concatenations, and other expressions are reported as errors " +
                "because the value is embedded into the synthetic hint function name at " +
                "IR-pass time.",
            declarationLocation(property),
        )
    }

    /**
     * Reports the regex-violation error for a literal `collect[All]ModulePreviews(scope = ...)`
     * value that reached IR through const-folding.
     *
     * The FIR `CollectScopeCallChecker` catches inline string literals
     * (`scope = "has-hyphen"`) at analysis time, but a `const val` reference is
     * indistinguishable from a non-`const` `val` reference at FIR time and is therefore
     * left to the IR pass. By the time we get here the const-folded `IrConst<String>` is
     * a hand-written-literal-equivalent, so the same regex must apply — otherwise a
     * `private const val BAD = "has space"; collectModulePreviews(scope = BAD)` would
     * silently bypass validation and the synthetic `previewHint_<scope>` lookup would
     * either throw on `Name.identifier(...)` or land on an unrelated identifier.
     */
    private fun reportInvalidScopeError(property: IrProperty, callName: String, scope: String) {
        messageCollector.report(
            CompilerMessageSeverity.ERROR,
            "[ComposePreviewLab] $callName(scope = \"$scope\") is not a valid scope " +
                "identifier. Allowed characters: [A-Za-z0-9_]+ (the value is embedded into " +
                "the synthetic previewHint_<scope> function name). This usually means a " +
                "`const val` referenced as `scope = MY_CONST` evaluates to an invalid " +
                "string at compile time.",
            declarationLocation(property),
        )
    }

    /**
     * Outcome of inspecting the `scope = ...` argument of a
     * `collect[All]ModulePreviews(...)` call site.
     */
    private sealed interface ScopeArgResult {
        /** The user omitted the argument (`arguments[0] == null`); use the runtime default. */
        data object Default : ScopeArgResult

        /** The user supplied an `IrConst<String>`; the value is the chosen scope. */
        data class Literal(val value: String) : ScopeArgResult

        /** The user supplied a non-literal expression (named const, concat, ...). */
        data class NonLiteral(val expr: IrExpression) : ScopeArgResult
    }

    /**
     * Inspects the `scope` argument on a `collect[All]ModulePreviews(...)` IR call.
     *
     * `IrCall.arguments[0]` is the first regular value parameter for top-level functions;
     * `null` means the user omitted the argument (Kotlin compiler keeps that as the
     * sentinel for default-arg invocations all the way through to the IR pass).
     */
    private fun resolveScopeArg(call: IrCall): ScopeArgResult {
        val arg = call.arguments.getOrNull(0) ?: return ScopeArgResult.Default
        return when {
            arg is IrConst && arg.value is String -> ScopeArgResult.Literal(arg.value as String)
            else -> ScopeArgResult.NonLiteral(arg)
        }
    }
}
