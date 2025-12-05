package me.tbsten.compose.preview.lab.ksp.plugin

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import me.tbsten.compose.preview.lab.ksp.plugin.util.findAnnotation
import me.tbsten.compose.preview.lab.ksp.plugin.util.findArg
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.TextRange
import org.jetbrains.kotlin.com.intellij.psi.util.PsiUtilCore
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtFile

internal fun checkPreview(annotated: KSAnnotated): ValidPreview? {
    if (annotated !is KSFunctionDeclaration) return null
    val optionAnnotation =
        annotated.findAnnotation("me.tbsten.compose.preview.lab.ComposePreviewLabOption")
    val displayName = optionAnnotation?.findArg<String>("displayName")
    val ignore = optionAnnotation?.findArg<Boolean>("ignore")
    val id = optionAnnotation?.findArg<String>("id")
    if (ignore == true) return null
    return ValidPreview(
        previewFun = annotated,
        placeholderedDisplayName = displayName ?: "{{qualifiedName}}",
        placeholderedId = id ?: "{{qualifiedName}}",
    )
}

internal data class ValidPreview(
    val previewFun: KSFunctionDeclaration,
    val placeholderedDisplayName: String,
    val placeholderedId: String,
)

internal class CopyPreviewContext(val environment: KotlinCoreEnvironment)

internal fun copyPreview(context: CopyPreviewContext, preview: ValidPreview, codeGenerator: CodeGenerator): CopiedPreview {
    val startLineNumber = (preview.previewFun.location as? FileLocation)?.lineNumber
    val copied = CopiedPreview(
        packageName = preview.previewFun.packageName.asString(),
        baseName = preview.previewFun.simpleName.asString(),
        baseFile = preview.previewFun.containingFile
            ?: throw IllegalStateException("Preview containing file is null"),
        startLineNumber = startLineNumber,
        code = null,
        placeholderedDisplayName = preview.placeholderedDisplayName,
        placeholderedId = preview.placeholderedId,
    )

    val filePath = (
        preview.previewFun.containingFile?.filePath
            ?: throw IllegalStateException(
                "Can not copy Preview for Compose Preview Lab, because file path is null.\n" +
                    "    Preview fun name = ${preview.previewFun.simpleName.asString()} (${preview.previewFun.qualifiedName?.asString()})",
            )
        )
    val (psiFile, previewBody) =
        run {
            val virtualFile = requireNotNull(context.environment.findLocalFile(filePath))
            val psiFile = PsiUtilCore.getPsiFile(context.environment.project, virtualFile) as? KtFile
                ?: throw IllegalStateException(
                    "Can not copy Preview for Compose Preview Lab, because file is not KtFile.\n" +
                        "|  error details:\n" +
                        "|    file = ${virtualFile.name} (${virtualFile.path})\n" +
                        "|    fileType = ${
                            virtualFile.fileType.name +
                                " (${
                                    PsiUtilCore.getPsiFile(
                                        context.environment.project,
                                        virtualFile,
                                    )::class.simpleName?.removeSuffix("Impl")
                                })"
                        }\n" +
                        "|",
                )
            val functionElement =
                FunctionCollector.visitPsiFile(file = psiFile, funName = copied.baseName)
                    .single()

            val bodyExp = functionElement.bodyExpression
            val bodyText =
                if (bodyExp is KtBlockExpression) {
                    val range = TextRange(
                        bodyExp.lBrace!!.textRange.endOffset - bodyExp.textRange.startOffset,
                        bodyExp.rBrace!!.textRange.startOffset - bodyExp.textRange.startOffset,
                    )
                    bodyExp.text.substring(range.startOffset, range.endOffset)
                } else if (bodyExp != null) {
                    bodyExp.text
                } else {
                    "// FIXME Warn: ${functionElement.name} has no body"
                }
            psiFile to bodyText
        }

    codeGenerator.createNewFile(
        dependencies = Dependencies(
            aggregating = false,
            sources = buildList { preview.previewFun.containingFile?.let { add(it) } }.toTypedArray(),
        ),
        packageName = copied.packageName,
        fileName = "Copied${copied.baseName}",
    ).bufferedWriter().use { writer ->
        writer.appendLine("package ${copied.packageName}")
        writer.appendLine()
        psiFile.importDirectives.forEach {
            writer.appendLine(it.text)
        }
        if (!psiFile.importDirectives.any { it.importedFqName?.asString() == "androidx.compose.runtime.Composable" }) {
            writer.appendLine("import androidx.compose.runtime.Composable")
        }
        writer.appendLine()
        writer.appendLine("/**")
        writer.appendLine(" * base: [${copied.baseName}]")
        writer.appendLine(" */")
        writer.appendLine("@Composable")
        writer.appendLine("internal fun ${copied.copyName}() {")
        writer.appendLine(previewBody)
        writer.appendLine("}")
        writer.appendLine()
    }

    return copied.copy(
        code = previewBody,
    )
}

internal data class CopiedPreview(
    val packageName: String,
    val baseName: String,
    val baseFile: KSFile,
    val startLineNumber: Int?,
    val code: String?,
    val placeholderedDisplayName: String,
    val placeholderedId: String,
) {
    val fullBaseName = "$packageName.$baseName"
    val copyName = "__copied__$baseName"
    val fullCopyName = "$packageName.$copyName"

    val displayName = placeholderedDisplayName
        .replace("{{package}}", packageName)
        .replace("{{simpleName}}", baseName)
        .replace("{{qualifiedName}}", fullBaseName)

    val id = placeholderedId
        .replace("{{package}}", packageName)
        .replace("{{simpleName}}", baseName)
        .replace("{{qualifiedName}}", fullBaseName)
}
