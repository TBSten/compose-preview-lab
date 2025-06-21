package me.tbsten.compose.preview.lab.ksp.plugin

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.validate

private const val PreviewAnnotation = "org.jetbrains.compose.ui.tooling.preview.Preview"

internal class ComposePreviewLabKspProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : SymbolProcessor {
    private var isExecuted = false
    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (isExecuted) return emptyList()
        isExecuted = true

        val previewsListPackage = options["composePreviewLab.previewsListPackage"]
            ?: throw IllegalStateException("ksp arg `composePreviewLab.previewsListPackage` is not set.")

        val publicPreviewList =
            options["composePreviewLab.publicPreviewList"]?.lowercase() == "true"

        val previews = resolver.getSymbolsWithAnnotation(PreviewAnnotation)
        if (previews.any { !it.validate() }) return previews.toList()

        val copiedPreviews = mutableListOf<CopiedPreview>()
        previews.forEach { preview ->
            val validPreview =
                checkPreview(preview)

            if (validPreview != null) {
                copyPreview(validPreview, codeGenerator = codeGenerator)
                    .also { copiedPreviews += it }
            }
        }

        generateList(
            previews = copiedPreviews.toList(),
            codeGenerator = codeGenerator,
            previewsListPackage = previewsListPackage,
            publicPreviewList = publicPreviewList,
        )
        return emptyList()
    }
}
