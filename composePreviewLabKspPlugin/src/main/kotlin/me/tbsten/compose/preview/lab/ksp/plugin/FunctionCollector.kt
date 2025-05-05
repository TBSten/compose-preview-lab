package me.tbsten.compose.preview.lab.ksp.plugin

import org.jetbrains.kotlin.com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

internal class FunctionCollector(
    private val funName: String,
) : KtTreeVisitorVoid() {
    val requiredDeclarations = mutableListOf<KtNamedFunction>()

    override fun visitDeclaration(dcl: KtDeclaration) {
        val shouldBeCopied = when (dcl) {
            is KtNamedFunction -> dcl.isTopLevel && dcl.name == funName

            else -> false
        }

        if (!shouldBeCopied) {
            return super.visitDeclaration(dcl)
        }
        (dcl as? KtNamedFunction)
            ?.let(requiredDeclarations::add)
    }

    companion object {
        fun visitPsiFile(file: PsiFile, funName: String): List<KtNamedFunction> {
            val collector = FunctionCollector(funName = funName)
            file.accept(collector)
            return collector.requiredDeclarations.toList()
        }
    }
}
