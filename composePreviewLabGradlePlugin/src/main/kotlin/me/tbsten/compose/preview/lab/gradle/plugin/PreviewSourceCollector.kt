package me.tbsten.compose.preview.lab.gradle.plugin

import org.jetbrains.kotlin.com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

class PreviewSourceCollector private constructor() : KtTreeVisitorVoid() {
    companion object {
        fun visitPsiFile(psiFile: PsiFile): List<KtNamedFunction> {
            val collector = PreviewSourceCollector()
            psiFile.accept(collector)
            return collector.includePreviewDeclarations
        }
    }

    private val includePreviewDeclarations = mutableListOf<KtNamedFunction>()

    override fun visitDeclaration(dcl: KtDeclaration) {
        if (dcl is KtNamedFunction && dcl.isPreviewAndComposable()) {
            includePreviewDeclarations.add(dcl)
        } else {
            return super.visitDeclaration(dcl)
        }
    }
}

private fun KtNamedFunction.isPreviewAndComposable(): Boolean {
    val hasPreviewAnnotation =
        annotationEntries.any { it.text.startsWith("@Preview") }
    val hasComposableAnnotation = annotationEntries.any { it.text == "@Composable" }

    return hasPreviewAnnotation && hasComposableAnnotation
}
