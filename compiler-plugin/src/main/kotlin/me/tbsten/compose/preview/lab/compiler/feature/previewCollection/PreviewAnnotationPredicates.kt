package me.tbsten.compose.preview.lab.compiler.feature.previewCollection

import me.tbsten.compose.preview.lab.compiler.PreviewLabConstants
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate

/**
 * FIR `LookupPredicate`s shared by the `previewCollection` feature.
 *
 * Both the hint generator (`hintGeneration/GeneratePreviewHintFir`) and the marker
 * generator (`markerGeneration/GeneratePreviewHintMarkerFir`) walk the same set of
 * `@Preview`-annotated symbols. Centralising the predicate construction here keeps the
 * annotation FQN list (CMP + Android) in one place — anything that needs to react to
 * `@Preview` registers these predicates rather than redeclaring them.
 *
 * **Sample registration** (inside a `FirDeclarationGenerationExtension`):
 * ```kotlin
 * override fun FirDeclarationPredicateRegistrar.registerPredicates() {
 *     register(PreviewAnnotationPredicates.previewPredicate)
 *     register(PreviewAnnotationPredicates.optionPredicate)
 * }
 * ```
 *
 * The actual `getSymbolsByPredicate(...)` lookup is performed in
 * [HintEntriesProvider] so that both generators share the same resolved symbol list
 * (= the SSoT for "which @Preview functions exist in this session").
 */
internal object PreviewAnnotationPredicates {

    /**
     * Predicate that locates `@Preview` functions — targets both the CMP and Android
     * `@Preview` annotation FQNs.
     */
    val previewPredicate: LookupPredicate = LookupPredicate.create {
        annotated(
            PreviewLabConstants.CMP_PREVIEW_ANNOTATION_FQN,
            PreviewLabConstants.ANDROID_PREVIEW_ANNOTATION_FQN,
        )
    }

    /**
     * Auxiliary predicate that pulls `@ComposePreviewLabOption` into the FIR
     * predicate-based provider's resolved annotation set.
     *
     * Without this, the option annotation's `annotationTypeRef` stays unresolved on
     * `@Preview` symbols during STATUS phase: `toAnnotationClassIdSafe(session)` then
     * returns null for the option annotation, even after
     * `lazyResolveToPhase(ANNOTATION_ARGUMENTS)`. Registering the predicate signals the
     * resolver that this annotation type must be eagerly resolved on every symbol it
     * appears on, so option-argument readers (e.g. `ignore = true`, `collectScopes`)
     * see the resolved arguments reliably.
     */
    val optionPredicate: LookupPredicate = LookupPredicate.create {
        annotated(PreviewLabConstants.COMPOSE_PREVIEW_LAB_OPTION_FQN)
    }
}
