package me.tbsten.compose.preview.lab.ksp.plugin

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

public class ComposePreviewLabKspProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = ComposePreviewLabKspProcessor(
        codeGenerator = environment.codeGenerator,
        logger = environment.logger,
        options = environment.options,
    )
}
