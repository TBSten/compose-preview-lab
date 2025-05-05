package me.tbsten.compose.preview.lab.ksp.plugin

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies


internal fun generateList(
    previews: List<CopiedPreview>,
    codeGenerator: CodeGenerator,
    previewsListPackage: String,
) {
    codeGenerator.createNewFile(
        dependencies = Dependencies(
            aggregating = false,
            sources = previews.map { it.baseFile }.toTypedArray()
        ),
        packageName = previewsListPackage,
        fileName = "Previews",
    ).bufferedWriter().use {
        it.appendLine("package $previewsListPackage")
        it.appendLine()
        it.appendLine("import me.tbsten.compose.preview.lab.me.CollectedPreview")
        it.appendLine()
        it.appendLine("val previews = listOf<CollectedPreview>(")
        previews.forEach { preview ->
            it.appendLine("    // ${preview.fullBaseName}")
            it.appendLine("    CollectedPreview(")
            it.appendLine("        displayName = \"\"\"${preview.displayName}\"\"\"")
            it.appendLine("    ) { ${preview.fullCopyName}() },")
        }
        it.appendLine(")")
        it.appendLine()
    }
}
