package me.tbsten.compose.preview.lab.compiler.feature.transformPrivatePreviewToInternal.fir.visibilityPromotion

import me.tbsten.compose.preview.lab.compiler.compat.isFirFunction
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.ANDROID_PREVIEW_ANNOTATION_FQN
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.CMP_PREVIEW_ANNOTATION_FQN
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirDeclarationStatus
import org.jetbrains.kotlin.fir.declarations.FirMemberDeclaration
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.extensions.FirStatusTransformerExtension
import org.jetbrains.kotlin.fir.extensions.transform
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.FirResolvedTypeRef
import org.jetbrains.kotlin.name.ClassId

/**
 * Widens the visibility of `@Preview`-annotated `private` functions to `internal`.
 *
 * This makes the function reachable from the synthetic `PreviewList` generated in the
 * same module.
 */
class PreviewLabFirStatusTransformerExtension(session: FirSession) : FirStatusTransformerExtension(session) {

    private val previewAnnotations = listOf(
        ClassId.topLevel(CMP_PREVIEW_ANNOTATION_FQN),
        ClassId.topLevel(ANDROID_PREVIEW_ANNOTATION_FQN),
    )

    /**
     * Returns `true` when the declaration is a `private` function annotated with `@Preview`.
     *
     * Example — returns `true` for:
     * ```kotlin
     * @Preview
     * private fun MyButton() { ... }
     * ```
     *
     * Returns `false` for non-functions, non-private declarations, or functions without
     * a Compose `@Preview` annotation.
     */
    override fun needTransformStatus(declaration: FirDeclaration): Boolean {
        if (!declaration.isFirFunction()) return false
        if (declaration !is FirMemberDeclaration) return false
        if (declaration.visibility != Visibilities.Private) return false
        return previewAnnotations.any { classId -> declaration.hasPreviewAnnotation(classId) }
    }

    /**
     * Widens the visibility of a `private` `@Preview` function to `internal`.
     *
     * **Before**:
     * ```kotlin
     * @Preview
     * private fun MyButton() { ... }  // Visibilities.Private
     * ```
     *
     * **After**:
     * ```kotlin
     * @Preview
     * internal fun MyButton() { ... }  // Visibilities.Internal
     * ```
     *
     * `internal` is the minimum visibility needed for the synthetically generated
     * `PreviewList` in the same module to call the function directly. `public` would
     * change the library API surface, so `internal` is used instead.
     */
    override fun transformStatus(status: FirDeclarationStatus, declaration: FirDeclaration): FirDeclarationStatus =
        status.transform(visibility = Visibilities.Internal)

    private fun FirDeclaration.hasPreviewAnnotation(classId: ClassId): Boolean {
        // Fast path: the annotation class is on the classpath and resolvable through the session.
        if (hasAnnotation(classId, session)) return true
        // Fallback: the annotation class is not on the classpath, so read the ClassId
        // directly from the annotation's type reference instead.
        return annotations.any { it.matchesClassId(classId) }
    }

    private fun FirAnnotation.matchesClassId(classId: ClassId): Boolean {
        val typeRef = annotationTypeRef as? FirResolvedTypeRef ?: return false
        val coneType = typeRef.coneType as? ConeClassLikeType ?: return false
        return coneType.lookupTag.classId == classId
    }
}
