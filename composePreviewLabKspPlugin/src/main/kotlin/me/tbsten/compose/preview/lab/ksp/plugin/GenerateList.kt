package me.tbsten.compose.preview.lab.ksp.plugin

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import java.nio.file.Paths
import kotlin.io.path.relativeTo
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream

@OptIn(ExperimentalSerializationApi::class)
internal fun generateList(
    previews: List<CopiedPreview>,
    codeGenerator: CodeGenerator,
    previewsListPackage: String,
    projectRootPath: String?,
    publicPreviewList: Boolean,
) {
    codeGenerator.createNewFile(
        dependencies = Dependencies(
            aggregating = false,
            sources = previews.map { it.baseFile }.toTypedArray(),
        ),
        packageName = previewsListPackage,
        fileName = "Previews",
    ).bufferedWriter().use {
        it.appendLine("package $previewsListPackage")
        it.appendLine()
        it.appendLine("import me.tbsten.compose.preview.lab.CollectedPreview")
        it.appendLine()
        it.appendLine(
            "${if (publicPreviewList) "public" else "internal"} " +
                "val previews = listOf<CollectedPreview>(",
        )
        previews.forEach { preview ->
            it.appendLine("    // ${preview.fullBaseName}")
            it.appendLine("    CollectedPreview(")
            it.appendLine("        displayName = \"\"\"${preview.displayName}\"\"\",")
            val filePath = if (projectRootPath != null) {
                Paths.get(preview.baseFile.filePath).relativeTo(Paths.get(projectRootPath))
            } else {
                preview.baseFile.filePath
            }
            it.appendLine("        filePath = \"\"\"$filePath\"\"\",")
            it.appendLine("        startLineNumber = ${preview.startLineNumber ?: "null"},")
            it.appendLine("    ) { ${preview.fullCopyName}() },")
        }
        it.appendLine(")")
        it.appendLine()
    }

    codeGenerator.createNewFile(
        dependencies = Dependencies(
            aggregating = false,
            sources = previews.map { it.baseFile }.toTypedArray(),
        ),
        packageName = "",
        fileName = "previews",
        extensionName = "json",
    ).use { outputStream ->
        Json.encodeToStream(
            CollectedPreviewJsonData(
                previewsListPackage = previewsListPackage,
            ),
            outputStream,
        )
    }
}

@Serializable
data class CollectedPreviewJsonData(val previewsListPackage: String)
