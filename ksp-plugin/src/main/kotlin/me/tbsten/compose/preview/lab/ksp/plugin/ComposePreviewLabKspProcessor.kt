package me.tbsten.compose.preview.lab.ksp.plugin

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.validate
import me.tbsten.compose.preview.lab.internal.KspArg
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment.Companion.getOrCreateApplicationEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment.ProjectEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.config.CompilerConfiguration

private const val AndroidPreviewAnnotation = "androidx.compose.ui.tooling.preview.Preview"
private const val CMPPreviewAnnotation = "org.jetbrains.compose.ui.tooling.preview.Preview"

// TODO Migrate to Kotlin Compiler Plugin
//  https://github.com/TBSten/compose-preview-lab/issues/52
internal class ComposePreviewLabKspProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : SymbolProcessor {
    private var isExecuted = false

    private val environment by lazy {
        val disposable = Disposer.newDisposable()
        val configuration = CompilerConfiguration()
        val appEnv = getOrCreateApplicationEnvironment(disposable, configuration)
        val projectEnv = ProjectEnvironment(disposable, appEnv, configuration)

        KotlinCoreEnvironment.createForProduction(
            projectEnvironment = projectEnv,
            configuration = configuration,
            EnvironmentConfigFiles.JVM_CONFIG_FILES,
        )
    }

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (isExecuted) return emptyList()
        isExecuted = true

        val previewsListPackage = options[KspArg.previewsListPackage]
        if (previewsListPackage == null) {
            throw NotConfiguredPreviewsListPackageException()
        } else if (previewsListPackage.isBlank()) {
            throw InvalidPreviewsListPackageException(previewsListPackage)
        }

        val publicPreviewList =
            options.optionIsTrue(KspArg.publicPreviewList)

        val projectRootPath = options[KspArg.projectRootPath]

        if (options.optionIsTrue(KspArg.generatePreviewList)) {
            val previews =
                resolver.getSymbolsWithAnnotation(CMPPreviewAnnotation) +
                    resolver.getSymbolsWithAnnotation(AndroidPreviewAnnotation)

            if (previews.any { !it.validate() }) return previews.toList()

            val copiedPreviews = mutableListOf<CopiedPreview>()
            previews.forEach { preview ->
                val validPreview =
                    checkPreview(preview)

                if (validPreview != null) {
                    copyPreview(CopyPreviewContext(environment = environment), validPreview, codeGenerator = codeGenerator)
                        .also { copiedPreviews += it }
                }
            }

            generateList(
                previews = copiedPreviews.toList(),
                codeGenerator = codeGenerator,
                previewsListPackage = previewsListPackage,
                publicPreviewList = publicPreviewList,
                projectRootPath = projectRootPath,
            )

            if (options[KspArg.generatePreviewAllList]?.lowercase() == "true") {
                prepareModuleForPreviewAllAggregate(
                    codeGenerator = codeGenerator,
                    previewsListPackage = previewsListPackage,
                )

                generatePreviewAll(
                    resolver = resolver,
                    codeGenerator = codeGenerator,
                    previewsListPackage = previewsListPackage,
                )
            }
        }
        return emptyList()
    }
}

private fun Map<String, String>.optionIsTrue(key: String) = this[key]?.lowercase() == "true"
