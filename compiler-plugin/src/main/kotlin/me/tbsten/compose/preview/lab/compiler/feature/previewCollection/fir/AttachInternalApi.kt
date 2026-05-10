package me.tbsten.compose.preview.lab.compiler.feature.previewCollection.fir

import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.utils.classIdOf
import me.tbsten.compose.preview.lab.compiler.utils.fir.buildSimpleAnnotation
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirCallableDeclaration
import org.jetbrains.kotlin.fir.declarations.FirClassLikeDeclaration

private val InternalApiClassId = classIdOf("me.tbsten.compose.preview.lab", "InternalComposePreviewLabApi")

private val SyntheticPreviewHintClassId = classIdOf("me.tbsten.compose.preview.lab", "SyntheticPreviewHint")

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
            InternalApiClassId.buildSimpleAnnotation(session),
            SyntheticPreviewHintClassId.buildSimpleAnnotation(session),
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
            InternalApiClassId.buildSimpleAnnotation(session),
            SyntheticPreviewHintClassId.buildSimpleAnnotation(session),
        ),
    )
    compat.getDeprecationsProviderCompat(this, session)?.let(::replaceDeprecationsProvider)
}
