package me.tbsten.compose.preview.lab.compiler.utils.fir

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.toFirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.constructType
import org.jetbrains.kotlin.name.ClassId

/**
 * Builds a no-arg [FirAnnotation] for this [ClassId].
 *
 * **Sample call**:
 * ```kotlin
 * val anno = InternalApiClassId.buildSimpleAnnotation(session)
 * ```
 *
 * **Result** (semantically):
 * ```kotlin
 * @me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
 * ```
 *
 * The annotation type ref is fully resolved so it can be appended to a synthesized
 * declaration's annotation list without further FIR resolution work. Annotations
 * with value arguments are out of scope — add a separate overload when needed.
 *
 * If the annotation symbol cannot be resolved from [session], the call fails fast
 * with a descriptive `IllegalStateException`. A `null` here signals an unrecoverable
 * environment problem (missing `:annotation` jar on the classpath, plugin/runtime
 * version mismatch, or a kctfork-style test that did not inline the annotation stub),
 * not a user code issue.
 */
internal fun ClassId.buildSimpleAnnotation(session: FirSession): FirAnnotation = buildAnnotation {
    val annoSymbol = session.symbolProvider.getClassLikeSymbolByClassId(this@buildSimpleAnnotation) as? FirRegularClassSymbol
        ?: error(
            "${this@buildSimpleAnnotation} annotation symbol is not resolvable from the FIR session. The Compose " +
                "Preview Lab `:annotation` module must be on the compilation classpath for the " +
                "per-declaration hint generator. If this fires from a kctfork-style test, ensure " +
                "the test base inlines the annotation stub for both `@InternalComposePreviewLabApi` " +
                "and `@SyntheticPreviewHint` (see `CompilerPluginTestBase` / `CompilerPluginJsTestBase`).",
        )

    annotationTypeRef = annoSymbol.constructType().toFirResolvedTypeRef()
    argumentMapping = buildAnnotationArgumentMapping {}
}
