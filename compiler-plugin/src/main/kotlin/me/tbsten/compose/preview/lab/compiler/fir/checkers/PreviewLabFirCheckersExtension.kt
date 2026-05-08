package me.tbsten.compose.preview.lab.compiler.fir.checkers

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirDeclarationChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirExpressionChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirNamedFunction
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall

/**
 * Single FIR `Checkers` extension that wires every `collectScope`-related semantic check
 * into the analysis phase, so invalid values surface in the IDE's red-squiggly highlighter
 * and at compile time *before* IR generation.
 *
 * Two checkers are registered — both call into the same diagnostic factories from
 * [CollectScopeErrors], so the validation logic itself lives in exactly one place even
 * though there are two trigger points (per-`@Preview` annotation argument and per-call
 * scope argument):
 *
 * - [CollectScopeAnnotationChecker] — validates `@ComposePreviewLabOption(collectScopes = [...])`.
 * - [CollectScopeCallChecker] — validates `collect[All]ModulePreviews(scope = ...)`.
 *
 * Sample registration result, per source file: every `@Preview` and every
 * `collect[All]ModulePreviews(...)` call passes through these checkers exactly once during
 * the FIR `CHECKERS` phase. Errors are reported through the `DiagnosticReporter` and end
 * up rendered by the `RootDiagnosticRendererFactory` registration in [CollectScopeErrors].
 */
internal class PreviewLabFirCheckersExtension(session: FirSession) : FirAdditionalCheckersExtension(session) {

    override val declarationCheckers: DeclarationCheckers = object : DeclarationCheckers() {
        override val simpleFunctionCheckers: Set<FirDeclarationChecker<FirNamedFunction>> = setOf(
            CollectScopeAnnotationChecker(),
        )
    }

    override val expressionCheckers: ExpressionCheckers = object : ExpressionCheckers() {
        override val functionCallCheckers: Set<FirExpressionChecker<FirFunctionCall>> = setOf(
            CollectScopeCallChecker(),
        )
    }
}
