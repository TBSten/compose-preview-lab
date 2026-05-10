package me.tbsten.compose.preview.lab.compiler.feature.previewCollection.fir.scopeValidation

import me.tbsten.compose.preview.lab.compiler.PreviewLabConstants
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirDeclarationChecker
import org.jetbrains.kotlin.fir.declarations.FirNamedFunction
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirAnnotationCall
import org.jetbrains.kotlin.fir.expressions.FirCall
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirLiteralExpression
import org.jetbrains.kotlin.fir.expressions.FirNamedArgumentExpression
import org.jetbrains.kotlin.fir.expressions.FirVarargArgumentsExpression
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.coneType

/**
 * Validates `@ComposePreviewLabOption(collectScopes = [...])` against the regex
 * `[A-Za-z0-9_]+` at FIR analysis time, before IR generation.
 *
 * **Sample input → reported diagnostic**:
 * ```kotlin
 * @ComposePreviewLabOption(collectScopes = ["good", "has-hyphen", "with space"])
 * @Preview fun bar() {}
 * ```
 * - Reports [CollectScopeErrors.INVALID_COLLECT_SCOPE_VALUE] on the `"has-hyphen"`
 *   literal element.
 * - Reports [CollectScopeErrors.INVALID_COLLECT_SCOPE_VALUE] on the `"with space"`
 *   literal element.
 * - `"good"` is left alone.
 *
 * Reporting per-element (rather than once for the whole `Array<String>`) lets the IDE
 * underline the exact bad string instead of the entire array literal, so the user can
 * fix one value without re-reading the others.
 */
internal class CheckCollectScopeAnnotation : FirDeclarationChecker<FirNamedFunction>(MppCheckerKind.Common) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirNamedFunction) {
        val annotation = declaration.annotations.firstOrNull { it.isComposePreviewLabOption() } ?: return
        val collectScopesArg = annotation.findCollectScopesArgument() ?: return
        val elements = collectScopesArg.collectStringLiteralElements() ?: return
        for ((elementSource, value) in elements) {
            if (!PreviewLabConstants.SCOPE_VALIDATION_REGEX.matches(value)) {
                reporter.reportOn(
                    elementSource ?: collectScopesArg.source,
                    CollectScopeErrors.INVALID_COLLECT_SCOPE_VALUE,
                    value,
                    context,
                )
            }
        }
    }

    private fun FirAnnotation.isComposePreviewLabOption(): Boolean =
        annotationTypeRef.coneType.classId == PreviewLabConstants.COMPOSE_PREVIEW_LAB_OPTION_CLASS_ID

    /**
     * Finds the `collectScopes` argument expression off [this] annotation.
     *
     * Resolution order:
     * 1. `argumentMapping.mapping[collectScopes]` — the canonical resolved-by-name lookup,
     *    populated after BODY_RESOLVE for properly-resolved annotation calls.
     * 2. Fallback walk of `argumentList.arguments` — for the rare case where the mapping
     *    is empty (observed for inherit-classpath kctfork builds and some predicate
     *    re-entry paths). Recognises both `FirNamedArgumentExpression(name = ...)` and
     *    a positional argument expression at parameter index 3 (the `Array<String>`
     *    `collectScopes` slot — the per-element string-literal validation happens
     *    later, in [collectStringLiteralElements]).
     */
    private fun FirAnnotation.findCollectScopesArgument(): FirExpression? {
        val mapped = (this as? FirAnnotationCall)
            ?.argumentMapping?.mapping?.get(PreviewLabConstants.COLLECT_SCOPE_NAME)
        if (mapped != null) return mapped

        val annotationCall = this as? FirAnnotationCall ?: return null
        for ((index, argument) in annotationCall.argumentList.arguments.withIndex()) {
            when (argument) {
                is FirNamedArgumentExpression ->
                    if (argument.name == PreviewLabConstants.COLLECT_SCOPE_NAME) return argument.expression
                else ->
                    if (index == CollectScopesParameterIndex) return argument
            }
        }
        return null
    }

    /**
     * Walks the array-shaped annotation argument and pairs each string-literal element
     * with its FIR source location for per-element diagnostic placement.
     *
     * Returns null if the argument is not an array shape we recognise. Annotation
     * `Array<String>` arguments arrive as either `FirVarargArgumentsExpression` (the
     * binary metadata representation) or `FirCollectionLiteral` (`["a", "b"]` source
     * syntax in Kotlin 2.3.x). Both expose their elements via the same
     * `arguments: List<FirExpression>` shape walked here.
     */
    private fun FirExpression.collectStringLiteralElements(): List<Pair<KtSourceElement?, String>>? {
        val elementExpressions = when (val expr = this) {
            is FirVarargArgumentsExpression -> expr.arguments
            is FirCall -> expr.argumentList.arguments
            else -> return null
        }
        return elementExpressions.mapNotNull { element ->
            val literal = element as? FirLiteralExpression ?: return@mapNotNull null
            val value = literal.value as? String ?: return@mapNotNull null
            literal.source to value
        }
    }

    companion object {
        /**
         * Index of `collectScopes` in the
         * `@ComposePreviewLabOption(displayName, ignore, id, collectScopes)` parameter
         * list — fallback when [findCollectScopesArgument] cannot use the resolved
         * argument mapping (e.g. mapping is empty and arguments arrive positionally).
         */
        private const val CollectScopesParameterIndex: Int = 3
    }
}
