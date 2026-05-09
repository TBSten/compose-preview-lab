package me.tbsten.compose.preview.lab.compiler.fir.checkers

import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactory1
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers
import org.jetbrains.kotlin.psi.KtElement

/**
 * `KtDiagnosticFactory*` declarations + their IDE renderer for the `collectScope`
 * validation suite.
 *
 * Two complementary factories are exposed:
 * - [INVALID_COLLECT_SCOPE_VALUE] — fires per offending element when the value does not
 *   match `[A-Za-z0-9_]+`. Triggered from `@ComposePreviewLabOption(collectScopes = [...])`
 *   (annotation side) and from `collect[All]ModulePreviews(scope = ...)` (call side).
 * - [NON_LITERAL_COLLECT_SCOPE] — fires when a `collect[All]ModulePreviews(scope = ...)`
 *   call site passes an expression that is not a compile-time string literal.
 *
 * Rationale for the direct `KtDiagnosticFactory1(...)` constructor instead of the more
 * common `by error1<...>()` delegate: in Kotlin 2.3.21 `error1` is declared with
 * `context(...)` parameters, so the delegate path requires `-Xcontext-parameters` enabled
 * for any code that uses it; passing the same arguments directly through the public
 * constructor sidesteps that without losing any expressivity.
 *
 * The `KtDiagnosticsContainer` base class plus [getRendererFactory] override are the
 * canonical Kotlin 2.3+ shape for plugin-defined diagnostics: the framework discovers the
 * renderer by walking from each factory back to its container — there is no global
 * `RootDiagnosticRendererFactory.registerFactory(...)` call any more.
 */
internal object CollectScopeErrors : KtDiagnosticsContainer() {
    /**
     * `[A-Za-z0-9_]+` mismatch on a per-`@Preview` `collectScopes` element or on a
     * `collect[All]ModulePreviews(scope = ...)` argument.
     */
    val INVALID_COLLECT_SCOPE_VALUE: KtDiagnosticFactory1<String> = KtDiagnosticFactory1(
        "INVALID_COLLECT_SCOPE_VALUE",
        Severity.ERROR,
        SourceElementPositioningStrategies.DEFAULT,
        KtElement::class,
        Renderer,
    )

    /**
     * `collect[All]ModulePreviews(scope = ...)` call site that passes an expression which
     * cannot become an `IrConst<String>` by the IR-pass time (string concatenation, a
     * call returning a String, ...).
     */
    val NON_LITERAL_COLLECT_SCOPE: KtDiagnosticFactory1<String> = KtDiagnosticFactory1(
        "NON_LITERAL_COLLECT_SCOPE",
        Severity.ERROR,
        SourceElementPositioningStrategies.DEFAULT,
        KtElement::class,
        Renderer,
    )

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = Renderer

    /**
     * Maps the diagnostic factories above to their human-readable messages. The
     * framework looks up the message at render time (IDE highlighter, `kotlinc` console,
     * `gradle build` log) by walking the factory → container → renderer chain.
     *
     * `MAP` is initialised lazily by `KtDiagnosticFactoryToRendererMap("name") { ... }`,
     * which breaks the otherwise-cyclic dependency between the factory definitions
     * (which reference [Renderer]) and the map population (which references the
     * factories): the lambda runs only when `MAP` is first accessed, by which point
     * both the factories and [Renderer] have completed their respective initialisations.
     */
    object Renderer : BaseDiagnosticRendererFactory() {
        // ktlint property-naming rule pushes for camelCase, but the property name is
        // forced to `MAP` by the `BaseDiagnosticRendererFactory` base class — overriding
        // any other name would be a no-op.
        @Suppress("ktlint:standard:property-naming")
        override val MAP: KtDiagnosticFactoryToRendererMap by KtDiagnosticFactoryToRendererMap("ComposePreviewLab") { map ->
            map.put(
                INVALID_COLLECT_SCOPE_VALUE,
                "[ComposePreviewLab] \"{0}\" is not a valid collectScope value. Allowed " +
                    "characters: [A-Za-z0-9_]+ (the value is embedded into the synthetic " +
                    "previewHint_<scope> function name).",
                CommonRenderers.STRING,
            )
            map.put(
                NON_LITERAL_COLLECT_SCOPE,
                "[ComposePreviewLab] {0}(scope = ...) accepts only a compile-time string " +
                    "constant — an inline string literal or a `const val` reference. " +
                    "Non-`const` vals, string concatenations, and other expressions are " +
                    "rejected because the value is embedded into the synthetic " +
                    "previewHint_<scope> function name at compile time.",
                CommonRenderers.STRING,
            )
        }
    }
}
