package me.tbsten.compose.preview.lab.ksp.plugin

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier

/**
 * Main entry point for generating field extension functions from @GenerateCombinedField annotations.
 *
 * This function processes all classes annotated with @GenerateCombinedField and delegates to:
 * - [generateCombinedFieldForClass] for data classes
 * - [generatePolymorphicFieldForSealedClass] for sealed interfaces/classes
 */
internal fun generateCombinedFields(resolver: Resolver, codeGenerator: CodeGenerator, logger: KSPLogger) {
    val annotatedClasses = resolver
        .getSymbolsWithAnnotation(GenerateCombinedFieldAnnotation)
        .filterIsInstance<KSClassDeclaration>()

    // Track which classes have already been processed to avoid duplicate generation
    val processedClasses = mutableSetOf<String>()
    // Queue of classes to process
    val toProcess = mutableListOf<KSClassDeclaration>()

    // Start with annotated classes
    toProcess.addAll(annotatedClasses)

    while (toProcess.isNotEmpty()) {
        val classDeclaration = toProcess.removeAt(0)
        val qualifiedName = classDeclaration.qualifiedName?.asString() ?: continue

        // Skip if already processed
        if (qualifiedName in processedClasses) continue
        processedClasses.add(qualifiedName)

        try {
            // Check if it's a sealed interface/class
            if (classDeclaration.modifiers.contains(Modifier.SEALED)) {
                generatePolymorphicFieldForSealedClass(classDeclaration, codeGenerator, logger)
            } else {
                // Find dependent data classes (those used in properties)
                val dependentClasses = findDependentDataClasses(classDeclaration, logger)

                // Add dependent classes to the processing queue
                dependentClasses.forEach { dependent ->
                    val dependentQualified = dependent.qualifiedName?.asString()
                    if (dependentQualified != null && dependentQualified !in processedClasses) {
                        toProcess.add(dependent)
                    }
                }

                // Generate field for current class
                generateCombinedFieldForClass(classDeclaration, codeGenerator, logger)
            }
        } catch (e: Exception) {
            logger.error(
                "Failed to generate field for ${classDeclaration.qualifiedName?.asString()}: ${e.message}",
                classDeclaration,
            )
        }
    }
}
