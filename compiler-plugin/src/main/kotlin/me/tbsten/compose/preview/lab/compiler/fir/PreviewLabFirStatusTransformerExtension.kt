package me.tbsten.compose.preview.lab.compiler.fir

import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirDeclarationStatus
import me.tbsten.compose.preview.lab.compiler.compat.isFirFunction
import org.jetbrains.kotlin.fir.declarations.FirMemberDeclaration
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.extensions.FirStatusTransformerExtension
import org.jetbrains.kotlin.fir.extensions.transform
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.FirResolvedTypeRef
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

/**
 * `@Preview` アノテーションが付与された private 関数の visibility を internal に変更する。
 *
 * これにより、生成される PreviewList から直接参照可能になる。
 */
class PreviewLabFirStatusTransformerExtension(session: FirSession) : FirStatusTransformerExtension(session) {

    private val previewAnnotations = listOf(
        ClassId.topLevel(FqName("org.jetbrains.compose.ui.tooling.preview.Preview")),
        ClassId.topLevel(FqName("androidx.compose.ui.tooling.preview.Preview")),
    )

    override fun needTransformStatus(declaration: FirDeclaration): Boolean {
        if (!declaration.isFirFunction()) return false
        if (declaration !is FirMemberDeclaration) return false
        if (declaration.visibility != Visibilities.Private) return false
        return previewAnnotations.any { classId -> declaration.hasPreviewAnnotation(classId) }
    }

    override fun transformStatus(status: FirDeclarationStatus, declaration: FirDeclaration): FirDeclarationStatus =
        status.transform(visibility = Visibilities.Internal)

    private fun FirDeclaration.hasPreviewAnnotation(classId: ClassId): Boolean {
        // session 経由で解決できる場合 (アノテーションクラスが classpath にある場合)
        if (hasAnnotation(classId, session)) return true
        // アノテーションクラスが classpath にない場合は型参照から ClassId を直接取得
        return annotations.any { it.matchesClassId(classId) }
    }

    private fun FirAnnotation.matchesClassId(classId: ClassId): Boolean {
        val typeRef = annotationTypeRef as? FirResolvedTypeRef ?: return false
        val coneType = typeRef.coneType as? ConeClassLikeType ?: return false
        return coneType.lookupTag.classId == classId
    }
}
