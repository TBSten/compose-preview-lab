package me.tbsten.compose.preview.lab.gradle.plugin

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.util.PsiUtilCore
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.incremental.deleteDirectoryContents
import org.jetbrains.kotlin.psi.KtFile
import java.io.File

interface PreviewWorkParameters : WorkParameters {
    val srcDirs: SetProperty<File>
    val modulePackage: Property<String>
    val generateDestinationDir: DirectoryProperty
}

abstract class ActionUsingKotlinCompiler : WorkAction<PreviewWorkParameters> {
    override fun execute() {
        val srcDirs = parameters.srcDirs.get()
        val modulePackage = parameters.modulePackage.get()
        val generateDestinationDir = parameters.generateDestinationDir.get().asFile
        regenerateModulePreviewScreenshots(
            srcDirs = srcDirs,
            modulePackage = modulePackage,
            generateDestinationDir = generateDestinationDir,
        )
    }

    private fun regenerateModulePreviewScreenshots(
        srcDirs: Set<File>,
        modulePackage: String,
        generateDestinationDir: File,
    ) {
        val generateDir = generateDestinationDir

        // regenerate screenshot tests each time to ensure that all tests are up-to-date.
        if (generateDir.exists()) {
            generateDir.deleteDirectoryContents()
        }

        val previews = mutableSetOf<ComposePreview>()

        // XxxPreviews.kt

        srcDirs.forEach { srcDir ->
            srcDir.walkTopDown()
                .filter { it.extension == "kt" }
                .forEach { file ->
                    val environment = KotlinCoreEnvironment.createForProduction(
                        Disposer.newDisposable(),
                        CompilerConfiguration(),
                        EnvironmentConfigFiles.METADATA_CONFIG_FILES,
                    )
                    val virtualFile = requireNotNull(environment.findLocalFile(file.path))
                    val psiFile = PsiUtilCore.getPsiFile(environment.project, virtualFile)

                    if (psiFile !is KtFile) error("non-kotlin file is not supported.")

                    val includePreviewDeclarations = PreviewSourceCollector.visitPsiFile(psiFile)
                    if (includePreviewDeclarations.isEmpty()) return@forEach

                    val packageDirective = psiFile.packageDirective
                    val packageName = packageDirective?.packageNameExpression?.text
                    val importDirectives = psiFile.importDirectives

                    val fileContent = StringBuilder().apply {
                        packageDirective?.let {
                            appendLine(it.text)
                            appendLine()
                        }
                        importDirectives.forEach {
                            appendLine(it.text)
                        }
                        appendLine()
                        includePreviewDeclarations.forEach { base ->
                            val copiedName = "copied_for_compose_preview_lab__${base.name}"
                            val copiedText = base.text
                                .replace(Regex("private(.+)fun")) {
                                    "public${it.groups[1]!!.value}fun"
                                }
                                .replace("fun ${base.name}", "fun $copiedName")
                            appendLine("// ${packageName}.${base.name}")
                            appendLine()
                            appendLine(copiedText)
                            appendLine()

                            previews.add(
                                ComposePreview(
                                    packageName = packageName!!,
                                    baseFunName = base.name!!,
                                    internalFunName = copiedName,
                                )
                            )
                        }
                    }.toString()

                    val outputTargetParentDir =
                        packageName?.let { File(generateDir, it.replace(".", "/")) }
                            ?: generateDir
                    val outputTargetFile = File(
                        outputTargetParentDir,
                        "${file.nameWithoutExtension}Previews.kt"
                    )

                    outputTargetParentDir.mkdirs()
                    outputTargetFile.writeText(fileContent)
                }
        }

        // GeneratedPreviews.kt
        File(
            generateDir,
            "${modulePackage.replace(".", "/")}/GeneratedPreviews.kt",
        ).apply {
            parentFile.mkdirs()
        }.bufferedWriter().use { writer ->
            writer.appendLine("package $modulePackage")
            writer.appendLine("")
            writer.appendLine("import androidx.compose.runtime.Composable")
            writer.appendLine("import androidx.compose.runtime.remember")
            writer.appendLine()
            writer.appendLine("@Composable")
            writer.appendLine("fun previews() = remember {")
            writer.appendLine("    sequence<Pair<String, @Composable () -> Unit>> {")
            previews.forEach { preview ->
                writer.appendLine("        yield(\"\"\"${preview.qualifiedBaseFunName}\"\"\" to { ${preview.qualifiedInternalFunName}() })")
            }
            writer.appendLine("    }")
            writer.appendLine("}")
        }
    }
}

private data class ComposePreview(
    val packageName: String,
    val baseFunName: String,
    val internalFunName: String,
) {
    val qualifiedBaseFunName = "$packageName.$baseFunName"
    val qualifiedInternalFunName = "$packageName.$internalFunName"
}
