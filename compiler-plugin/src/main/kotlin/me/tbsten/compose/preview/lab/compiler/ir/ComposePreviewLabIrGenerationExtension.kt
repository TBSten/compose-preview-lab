package me.tbsten.compose.preview.lab.compiler.ir

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.name.FqName

private val ANDROID_PREVIEW_ANNOTATION = FqName("androidx.compose.ui.tooling.preview.Preview")
private val CMP_PREVIEW_ANNOTATION = FqName("org.jetbrains.compose.ui.tooling.preview.Preview")
private val COMPOSE_PREVIEW_LAB_OPTION_ANNOTATION = FqName("me.tbsten.compose.preview.lab.ComposePreviewLabOption")

internal class ComposePreviewLabIrGenerationExtension(
    private val previewsListPackage: String,
    private val publicPreviewList: Boolean,
    private val projectRootPath: String?,
    private val generatePreviewAllList: Boolean,
) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        // Phase 1: Collect all @Preview functions
        val previewMetadataList = collectPreviews(moduleFragment)

        if (previewMetadataList.isEmpty() && !generatePreviewAllList) return

        // Phase 2: Generate PreviewList object
        if (previewMetadataList.isNotEmpty()) {
            PreviewListIrGenerator(
                pluginContext = pluginContext,
                moduleFragment = moduleFragment,
                previewsListPackage = previewsListPackage,
                publicPreviewList = publicPreviewList,
            ).generate(previewMetadataList)
        }

        // Phase 3: Generate PreviewAllList (multi-module aggregation)
        if (generatePreviewAllList) {
            PreviewAllListIrGenerator(
                pluginContext = pluginContext,
                moduleFragment = moduleFragment,
                previewsListPackage = previewsListPackage,
                hasPreviews = previewMetadataList.isNotEmpty(),
            ).generate()
        }
    }

    private fun collectPreviews(moduleFragment: IrModuleFragment): List<PreviewMetadata> {
        val previews = mutableListOf<PreviewMetadata>()

        for (file in moduleFragment.files) {
            for (declaration in file.declarations) {
                if (declaration !is IrSimpleFunction) continue
                if (!declaration.hasPreviewAnnotation()) continue

                val optionAnnotation = declaration.getOptionAnnotation()
                if (optionAnnotation?.ignore == true) continue

                val packageName = file.packageFqName.asString()
                val simpleName = declaration.name.asString()
                val qualifiedName = if (packageName.isNotEmpty()) "$packageName.$simpleName" else simpleName

                val displayName = (optionAnnotation?.displayName ?: "{{qualifiedName}}")
                    .resolvePlaceholders(packageName, simpleName, qualifiedName)
                val id = (optionAnnotation?.id ?: "{{qualifiedName}}")
                    .resolvePlaceholders(packageName, simpleName, qualifiedName)

                val filePath = resolveFilePath(file.fileEntry.name)
                val startLineNumber = extractStartLineNumber(declaration)
                val code = SourceCodeExtractor.extractFunctionBody(file.fileEntry, declaration)
                val kdoc = SourceCodeExtractor.extractKDoc(file.fileEntry, declaration)

                previews.add(
                    PreviewMetadata(
                        irFunction = declaration,
                        packageName = packageName,
                        simpleName = simpleName,
                        qualifiedName = qualifiedName,
                        displayName = displayName,
                        id = id,
                        filePath = filePath,
                        startLineNumber = startLineNumber,
                        code = code,
                        kdoc = kdoc,
                    ),
                )
            }
        }

        return previews
    }

    private fun IrSimpleFunction.hasPreviewAnnotation(): Boolean =
        hasAnnotation(ANDROID_PREVIEW_ANNOTATION) || hasAnnotation(CMP_PREVIEW_ANNOTATION)

    private data class OptionAnnotationData(
        val displayName: String?,
        val id: String?,
        val ignore: Boolean,
    )

    private fun IrSimpleFunction.getOptionAnnotation(): OptionAnnotationData? {
        val annotation = annotations.firstOrNull {
            it.type.classFqName == COMPOSE_PREVIEW_LAB_OPTION_ANNOTATION
        } ?: return null

        var displayName: String? = null
        var id: String? = null
        var ignore = false

        for (i in 0 until annotation.valueArgumentsCount) {
            when (annotation.symbol.owner.valueParameters.getOrNull(i)?.name?.asString()) {
                "displayName" -> displayName = annotation.getValueArgument(i)?.extractStringConst()
                "id" -> id = annotation.getValueArgument(i)?.extractStringConst()
                "ignore" -> ignore = annotation.getValueArgument(i)?.extractBooleanConst() ?: false
            }
        }

        return OptionAnnotationData(displayName = displayName, id = id, ignore = ignore)
    }

    private fun String.resolvePlaceholders(packageName: String, simpleName: String, qualifiedName: String): String =
        replace("{{package}}", packageName)
            .replace("{{simpleName}}", simpleName)
            .replace("{{qualifiedName}}", qualifiedName)

    private fun resolveFilePath(absolutePath: String): String? {
        if (projectRootPath == null) return absolutePath
        return try {
            java.nio.file.Paths.get(absolutePath)
                .toAbsolutePath()
                .let { path ->
                    java.nio.file.Paths.get(projectRootPath)
                        .toAbsolutePath()
                        .relativize(path)
                        .toString()
                }
        } catch (_: Exception) {
            absolutePath
        }
    }

    private fun extractStartLineNumber(function: IrSimpleFunction): Int? {
        val fileEntry = function.fileEntry
        val startOffset = function.startOffset
        if (startOffset < 0) return null
        return try {
            fileEntry.getLineNumber(startOffset) + 1 // 0-indexed to 1-indexed
        } catch (_: Exception) {
            null
        }
    }
}

private fun org.jetbrains.kotlin.ir.expressions.IrExpression.extractStringConst(): String? =
    (this as? org.jetbrains.kotlin.ir.expressions.IrConst)?.value as? String

private fun org.jetbrains.kotlin.ir.expressions.IrExpression.extractBooleanConst(): Boolean? =
    (this as? org.jetbrains.kotlin.ir.expressions.IrConst)?.value as? Boolean
