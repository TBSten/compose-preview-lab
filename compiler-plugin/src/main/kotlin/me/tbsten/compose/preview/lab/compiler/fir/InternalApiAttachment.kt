package me.tbsten.compose.preview.lab.compiler.fir

import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirCallableDeclaration
import org.jetbrains.kotlin.fir.declarations.FirClassLikeDeclaration
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.toFirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.constructType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

private val InternalApiClassId = ClassId(
    FqName("me.tbsten.compose.preview.lab"),
    Name.identifier("InternalComposePreviewLabApi"),
)

private val SyntheticPreviewHintClassId = ClassId(
    FqName("me.tbsten.compose.preview.lab"),
    Name.identifier("SyntheticPreviewHint"),
)

/**
 * Builds a no-arg FIR annotation for the given `ClassId`.
 *
 * **Sample call**: `buildSimpleAnnotation(session, InternalApiClassId)`
 *
 * **Result** (semantically):
 * ```kotlin
 * @me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
 * ```
 *
 * Both `@InternalComposePreviewLabApi` and `@SyntheticPreviewHint` are no-arg
 * annotations, so an empty `argumentMapping` is sufficient. The annotation type ref
 * is fully resolved so it can be appended to a synthesized declaration's annotation
 * list without further FIR resolution work.
 */
private fun buildSimpleAnnotation(session: FirSession, classId: ClassId): FirAnnotation = buildAnnotation {
    // Hard-fail when the annotation symbol can't be resolved: the FIR generator runs only
    // when the user has applied the Compose Preview Lab Gradle plugin, which always pulls
    // `:annotation` onto the compilation classpath as a transitive dependency. A null
    // here therefore signals one of two unrecoverable states — a plugin/runtime version
    // mismatch (different `:annotation` jar than the plugin expects) or a kctfork test
    // that does not inline the annotation stub. Both are bugs in the calling environment,
    // not in user code, so a clear stack trace is preferable to silently falling back
    // (which would disable `@InternalComposePreviewLabApi` BCV filtering and the
    // squatting guard simultaneously).
    val annoSymbol = session.symbolProvider.getClassLikeSymbolByClassId(classId) as? FirRegularClassSymbol
        ?: error(
            "$classId annotation symbol is not resolvable from the FIR session. The Compose " +
                "Preview Lab `:annotation` module must be on the compilation classpath for the " +
                "per-declaration hint generator. If this fires from a kctfork-style test, ensure " +
                "the test base inlines the annotation stub for both `@InternalComposePreviewLabApi` " +
                "and `@SyntheticPreviewHint` (see `CompilerPluginTestBase` / `CompilerPluginJsTestBase`).",
        )

    annotationTypeRef = annoSymbol.constructType().toFirResolvedTypeRef()
    argumentMapping = buildAnnotationArgumentMapping {}
}

/**
 * Attaches `@InternalComposePreviewLabApi` + `@SyntheticPreviewHint` to a synthesized FIR
 * class-like declaration (e.g. the per-`@Preview` marker interface).
 *
 * **Before**:
 * ```kotlin
 * @kotlin.Deprecated("...", level = HIDDEN)
 * public interface PreviewHintMarker_<sanitized_fqn>_<hash>
 * ```
 *
 * **After**:
 * ```kotlin
 * @kotlin.Deprecated("...", level = HIDDEN)
 * @me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
 * @me.tbsten.compose.preview.lab.SyntheticPreviewHint
 * public interface PreviewHintMarker_<sanitized_fqn>_<hash>
 * ```
 *
 * The two markers serve complementary roles:
 * * `@InternalComposePreviewLabApi` removes the marker class from BCV baselines on
 *   every CMP target (KLIB / JVM / Android), via the existing `nonPublicMarkers`
 *   filter in `apiValidation`.
 * * `@SyntheticPreviewHint` lets the IR-side `HintDiscovery` distinguish plugin-emitted
 *   hints from third-party declarations that happen to live in the same
 *   `me.tbsten.compose.preview.lab.hints` package, so a downstream
 *   `collectAllModulePreviews()` cannot be poisoned by namespace squatting.
 */
internal fun FirClassLikeDeclaration.markAsInternalSyntheticHint(session: FirSession, compat: CompatContext) {
    replaceAnnotations(
        annotations + listOf(
            buildSimpleAnnotation(session, InternalApiClassId),
            buildSimpleAnnotation(session, SyntheticPreviewHintClassId),
        ),
    )
    compat.getDeprecationsProviderCompat(this, session)?.let(::replaceDeprecationsProvider)
}

/**
 * Callable variant of [markAsInternalSyntheticHint]. Same semantics — see the
 * class-like overload above for the Before / After example and the rationale for
 * carrying both markers.
 *
 * Targets: the per-`@Preview` `previewHint_<scope>(...)` overloads emitted by
 * `PreviewHintFirGenerator`.
 */
internal fun FirCallableDeclaration.markAsInternalSyntheticHint(session: FirSession, compat: CompatContext) {
    replaceAnnotations(
        annotations + listOf(
            buildSimpleAnnotation(session, InternalApiClassId),
            buildSimpleAnnotation(session, SyntheticPreviewHintClassId),
        ),
    )
    compat.getDeprecationsProviderCompat(this, session)?.let(::replaceDeprecationsProvider)
}
