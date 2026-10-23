package me.tbsten.compose.preview.lab.ksp.plugin

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import me.tbsten.compose.preview.lab.ksp.plugin.autogenerate.AutoGenerateFieldProcessor

/**
 * KSP entry point of the opt-in `ksp-plugin` artifact.
 *
 * Preview collection itself is done by the Kotlin compiler plugin; this processor only backs
 * `@AutoGenerateField` and generates `PreviewLabField` factory functions for the annotated objects.
 *
 * ```kotlin
 * // build.gradle.kts
 * plugins { id("com.google.devtools.ksp") }
 * dependencies { add("kspCommonMainMetadata", "me.tbsten.compose.preview.lab:ksp-plugin:<version>") }
 * ```
 */
public class AutoGenerateFieldSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        AutoGenerateFieldSymbolProcessor(environment)
}

private class AutoGenerateFieldSymbolProcessor(environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private val processor = AutoGenerateFieldProcessor(environment.codeGenerator, environment.logger)
    private var isExecuted = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (isExecuted) return emptyList()
        isExecuted = true
        processor.process(resolver)
        return emptyList()
    }
}
