package me.tbsten.compose.preview.lab.compiler.fir

import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirCallableDeclaration
import org.jetbrains.kotlin.fir.declarations.FirClassLikeDeclaration
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.deserialization.toQualifiedPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildEnumEntryDeserializedAccessExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildLiteralExpression
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.toFirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.constructType
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.types.ConstantValueKind

/**
 * Internal-use message attached to every synthetic per-declaration hint declaration
 * (marker interface + `previewHint` overload). Surfaces in IDE deprecation tooltips
 * if a user somehow ends up referencing the symbol despite `level = HIDDEN`.
 */
private const val HiddenMessage = "This synthesized declaration should not be used directly"

/**
 * Builds a `@kotlin.Deprecated(message, level = DeprecationLevel.HIDDEN)` FIR annotation.
 *
 * **Sample call**: `createDeprecatedHiddenAnnotation(session)`
 *
 * **Result** (semantically):
 * ```kotlin
 * @kotlin.Deprecated(
 *     message = "This synthesized declaration should not be used directly",
 *     level = kotlin.DeprecationLevel.HIDDEN,
 * )
 * ```
 *
 * The annotation is built with a fully resolved type reference and an explicit
 * `argumentMapping` so it is ready to be appended to a synthesized declaration's
 * annotation list at FIR generation time, before the IDE / FIR checker reads back
 * deprecation info.
 *
 * Pattern adapted from Metro's [`createDeprecatedHiddenAnnotation`]
 * (https://github.com/ZacSweers/metro/blob/main/compiler/src/main/kotlin/dev/zacsweers/metro/compiler/fir/fir.kt).
 */
internal fun createDeprecatedHiddenAnnotation(session: FirSession): FirAnnotation = buildAnnotation {
    val deprecatedAnno = session.symbolProvider
        .getClassLikeSymbolByClassId(StandardClassIds.Annotations.Deprecated)
        as FirRegularClassSymbol

    annotationTypeRef = deprecatedAnno.constructType().toFirResolvedTypeRef()

    argumentMapping = buildAnnotationArgumentMapping {
        mapping[Name.identifier("message")] = buildLiteralExpression(
            source = null,
            kind = ConstantValueKind.String,
            value = HiddenMessage,
            setType = true,
        )
        mapping[Name.identifier("level")] = buildEnumEntryDeserializedAccessExpression {
            enumClassId = StandardClassIds.DeprecationLevel
            enumEntryName = Name.identifier("HIDDEN")
        }.toQualifiedPropertyAccessExpression(session)
    }
}

/**
 * Attaches `@Deprecated(message = ..., level = HIDDEN)` to a synthesized FIR
 * class-like declaration (e.g. the per-`@Preview` marker interface) and invalidates
 * the FIR / IDE deprecation cache so the new annotation actually takes effect for
 * subsequent name resolution.
 *
 * **Before**:
 * ```kotlin
 * public interface PreviewHintMarker_<sanitized_fqn>_<hash>
 * ```
 *
 * **After**:
 * ```kotlin
 * @kotlin.Deprecated(
 *     "This synthesized declaration should not be used directly",
 *     level = kotlin.DeprecationLevel.HIDDEN,
 * )
 * public interface PreviewHintMarker_<sanitized_fqn>_<hash>
 * ```
 *
 * `replaceDeprecationsProvider(...)` is the second half of the trick: without it
 * the IDE's deprecation cache stays populated from the pre-annotation declaration
 * snapshot, so the freshly attached `@Deprecated(HIDDEN)` would not gate name
 * resolution. [CompatContext.getDeprecationsProviderCompat] absorbs the 2.4.0-Beta2
 * receiver narrowing.
 */
internal fun FirClassLikeDeclaration.markAsDeprecatedHidden(session: FirSession, compat: CompatContext) {
    replaceAnnotations(annotations + listOf(createDeprecatedHiddenAnnotation(session)))
    compat.getDeprecationsProviderCompat(this, session)?.let(::replaceDeprecationsProvider)
}

/**
 * Attaches `@Deprecated(message = ..., level = HIDDEN)` to a synthesized FIR
 * callable declaration (e.g. the per-`@Preview` `previewHint` overload) and
 * invalidates the FIR / IDE deprecation cache. See [markAsDeprecatedHidden] above
 * for the class-like variant; the body is identical other than the receiver type.
 *
 * **Before**:
 * ```kotlin
 * public fun previewHint(value: PreviewHintMarker_<...>?): CollectedPreview
 * ```
 *
 * **After**:
 * ```kotlin
 * @kotlin.Deprecated(
 *     "This synthesized declaration should not be used directly",
 *     level = kotlin.DeprecationLevel.HIDDEN,
 * )
 * public fun previewHint(value: PreviewHintMarker_<...>?): CollectedPreview
 * ```
 */
internal fun FirCallableDeclaration.markAsDeprecatedHidden(session: FirSession, compat: CompatContext) {
    replaceAnnotations(annotations + listOf(createDeprecatedHiddenAnnotation(session)))
    compat.getDeprecationsProviderCompat(this, session)?.let(::replaceDeprecationsProvider)
}
