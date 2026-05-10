package me.tbsten.compose.preview.lab.compiler.fir.checkers

import me.tbsten.compose.preview.lab.compiler.PreviewLabConstants
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirExpressionChecker
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirLiteralExpression
import org.jetbrains.kotlin.fir.expressions.FirNamedArgumentExpression
import org.jetbrains.kotlin.fir.expressions.FirStringConcatenationCall
import org.jetbrains.kotlin.fir.expressions.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.name.FqName

/**
 * Validates `collect[All]ModulePreviews(scope = ...)` call sites at FIR analysis time.
 *
 * Two complementary checks fire here:
 * - **Invalid scope value** — string literal that does not match `[A-Za-z0-9_]+`.
 *   Reported as [CollectScopeErrors.INVALID_COLLECT_SCOPE_VALUE].
 * - **Non-literal `scope`** — string concatenation, function call, or other expression
 *   that clearly cannot inline to an `IrConst<String>` by IR-pass time. Reported as
 *   [CollectScopeErrors.NON_LITERAL_COLLECT_SCOPE].
 *
 * **Sample input → reported diagnostic**:
 * ```kotlin
 * val a by collectModulePreviews(scope = "has-hyphen")
 *                                    └─ INVALID_COLLECT_SCOPE_VALUE("has-hyphen")
 * val b by collectModulePreviews(scope = "ok" + "?")
 *                                    └─ NON_LITERAL_COLLECT_SCOPE("collectModulePreviews")
 * ```
 *
 * `const val` references are accepted: at FIR analysis time they remain as
 * `FirPropertyAccessExpression`, but they are inlined to `IrConst<String>` before the IR
 * pass, so they reach the embedding step as a literal. The checker leaves them alone here
 * (no false positive); if the inlined value violates the regex, the IR side reports it.
 */
internal class CollectScopeCallChecker : FirExpressionChecker<FirFunctionCall>(MppCheckerKind.Common) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirFunctionCall) {
        val callableSymbol = expression.toResolvedCallableSymbol() as? FirNamedFunctionSymbol ?: return
        val callableFqn = callableSymbol.callableId.asSingleFqName()
        if (callableFqn !in COLLECT_PREVIEWS_FQNS) return

        val scopeArgument = expression.findScopeArgument() ?: return
        val callName = callableFqn.shortName().asString()

        when (scopeArgument) {
            is FirLiteralExpression -> {
                val value = scopeArgument.value as? String ?: return
                if (!PreviewLabConstants.SCOPE_VALIDATION_REGEX.matches(value)) {
                    reporter.reportOn(
                        scopeArgument.source,
                        CollectScopeErrors.INVALID_COLLECT_SCOPE_VALUE,
                        value,
                        context,
                    )
                }
            }
            else -> {
                if (scopeArgument.isClearlyNonLiteral()) {
                    reporter.reportOn(
                        scopeArgument.source,
                        CollectScopeErrors.NON_LITERAL_COLLECT_SCOPE,
                        callName,
                        context,
                    )
                }
            }
        }
    }

    /**
     * Walks the call's argument list looking for the `scope` argument, regardless of
     * whether the user passed it positionally or by name. `collect[All]ModulePreviews`
     * declares a single parameter, so positional means `arguments[0]`.
     */
    private fun FirFunctionCall.findScopeArgument(): FirExpression? {
        for ((index, argument) in argumentList.arguments.withIndex()) {
            when (argument) {
                is FirNamedArgumentExpression -> if (argument.name.asString() == "scope") return argument.expression
                else -> if (index == 0) return argument
            }
        }
        return null
    }

    /**
     * Heuristic for "this expression cannot possibly inline to an `IrConst<String>` by
     * the IR phase". Conservatively returns false for property access (might be `const`),
     * lambdas, etc. — let the IR side flag those if they actually fail the literal
     * extraction.
     */
    private fun FirExpression.isClearlyNonLiteral(): Boolean = when (this) {
        is FirStringConcatenationCall -> true
        is FirFunctionCall -> true
        else -> false
    }

    companion object {
        private val COLLECT_PREVIEWS_FQNS: Set<FqName> = setOf(
            PreviewLabConstants.COLLECT_MODULE_PREVIEWS_FQN,
            PreviewLabConstants.COLLECT_ALL_MODULE_PREVIEWS_FQN,
        )
    }
}
