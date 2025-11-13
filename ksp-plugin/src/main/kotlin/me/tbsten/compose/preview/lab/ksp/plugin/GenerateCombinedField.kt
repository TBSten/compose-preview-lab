package me.tbsten.compose.preview.lab.ksp.plugin

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import java.io.OutputStreamWriter

private const val GenerateCombinedFieldAnnotation =
    "me.tbsten.compose.preview.lab.generatecombinedfield.GenerateCombinedField"

internal fun generateCombinedFields(resolver: Resolver, codeGenerator: CodeGenerator, logger: KSPLogger,) {
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
        } catch (e: Exception) {
            logger.error(
                "Failed to generate CombinedField for ${classDeclaration.qualifiedName?.asString()}: ${e.message}",
                classDeclaration,
            )
        }
    }
}

/**
 * Finds all data classes with companion objects that are used as property types
 * in the given class declaration
 */
private fun findDependentDataClasses(
    classDeclaration: KSClassDeclaration,
    logger: KSPLogger,
): List<KSClassDeclaration> {
    val properties = classDeclaration.primaryConstructor?.parameters ?: return emptyList()
    val dependentClasses = mutableListOf<KSClassDeclaration>()

    properties.forEach { param ->
        val paramType = param.type.resolve()
        val typeDeclaration = paramType.declaration as? KSClassDeclaration

        if (typeDeclaration != null &&
            typeDeclaration.modifiers.contains(Modifier.DATA)) {
            val hasCompanionObject = typeDeclaration.declarations
                .filterIsInstance<KSClassDeclaration>()
                .any { it.isCompanionObject }

            if (hasCompanionObject) {
                dependentClasses.add(typeDeclaration)
                logger.info("Found dependent data class: ${typeDeclaration.qualifiedName?.asString()}")
            }
        }
    }

    return dependentClasses
}

private fun generateCombinedFieldForClass(
    classDeclaration: KSClassDeclaration,
    codeGenerator: CodeGenerator,
    logger: KSPLogger,
) {
    // Validate it's a data class
    if (!classDeclaration.modifiers.contains(Modifier.DATA)) {
        logger.error("@GenerateCombinedField can only be applied to data classes", classDeclaration)
        return
    }

    // Find companion object
    val companionObject = classDeclaration.declarations
        .filterIsInstance<KSClassDeclaration>()
        .firstOrNull { it.isCompanionObject }

    if (companionObject == null) {
        logger.error("@GenerateCombinedField requires a companion object", classDeclaration)
        return
    }

    // Get primary constructor parameters
    val properties = classDeclaration.primaryConstructor?.parameters ?: run {
        logger.error("@GenerateCombinedField requires a primary constructor", classDeclaration)
        return
    }

    if (properties.isEmpty()) {
        logger.error("@GenerateCombinedField requires at least one property", classDeclaration)
        return
    }

    if (properties.size > 10) {
        logger.error("@GenerateCombinedField supports up to 10 properties", classDeclaration)
        return
    }

    val className = classDeclaration.simpleName.asString()
    val packageName = classDeclaration.packageName.asString()
    val qualifiedClassName = classDeclaration.qualifiedName?.asString() ?: className

    // Generate the file
    val fileName = "${className}GeneratedField"
    val file = codeGenerator.createNewFile(
        dependencies = Dependencies(true, classDeclaration.containingFile!!),
        packageName = packageName,
        fileName = fileName,
    )

    OutputStreamWriter(file).use { writer ->
        writer.write(generateCombinedFieldCode(className, qualifiedClassName, packageName, properties, logger))
    }
}

private fun generateCombinedFieldCode(
    className: String,
    qualifiedClassName: String,
    packageName: String,
    properties: List<com.google.devtools.ksp.symbol.KSValueParameter>,
    logger: KSPLogger,
): String {
    val fieldCount = properties.size
    val imports = mutableSetOf<String>()

    imports.add("me.tbsten.compose.preview.lab.field.MutablePreviewLabField")
    imports.add("me.tbsten.compose.preview.lab.field.CombinedField$fieldCount")
    imports.add("me.tbsten.compose.preview.lab.field.splitedOf")

    // Build field creators
    val fieldCreators = properties.mapIndexed { index, param ->
        val paramName = param.name?.asString() ?: "param$index"
        val paramType = param.type.resolve()
        val fieldCreator = generateFieldCreator(paramName, paramType, imports, logger)
        "    field${index + 1} = $fieldCreator"
    }

    // Build combine lambda parameters and call
    val combineParams = properties.mapIndexed { index, param ->
        param.name?.asString() ?: "param$index"
    }.joinToString(", ")

    val combineCall = properties.joinToString(", ") { param ->
        val paramName = param.name?.asString() ?: ""
        "$paramName = $paramName"
    }

    // Build split call
    val splitParams = properties.joinToString(", ") { param ->
        "it.${param.name?.asString()}"
    }

    val code = buildString {
        appendLine("package $packageName")
        appendLine()

        // Add imports
        imports.sorted().forEach { import ->
            appendLine("import $import")
        }
        appendLine()

        // Generate the extension function
        appendLine("/**")
        appendLine(" * Auto-generated field function for [$qualifiedClassName].")
        appendLine(" * ")
        appendLine(" * Creates a MutablePreviewLabField<$className> with CombinedField$fieldCount.")
        appendLine(" */")
        appendLine("fun $className.Companion.field(")
        appendLine("    label: String,")
        appendLine("    initialValue: $className,")
        appendLine("): MutablePreviewLabField<$className> = CombinedField$fieldCount(")
        appendLine("    label = label,")
        fieldCreators.forEach { appendLine(it + ",") }
        appendLine("    combine = { $combineParams ->")
        appendLine("        $className($combineCall)")
        appendLine("    },")
        appendLine("    split = { splitedOf($splitParams) },")
        appendLine(")")
    }

    return code
}

private fun generateFieldCreator(
    paramName: String,
    paramType: KSType,
    imports: MutableSet<String>,
    logger: KSPLogger,
): String {
    val typeName = paramType.declaration.simpleName.asString()
    val qualifiedTypeName = paramType.declaration.qualifiedName?.asString()

    // 1. Check if the type has @GenerateCombinedField annotation
    val isGeneratedField = paramType.declaration.annotations.any {
        it.annotationType.resolve().declaration.qualifiedName?.asString() == GenerateCombinedFieldAnnotation
    }

    if (isGeneratedField) {
        // Use the generated field function
        return "$typeName.field(label = \"$paramName\", initialValue = initialValue.$paramName)"
    }

    // 2. Check if it's a data class with companion object (potential for recursive field generation)
    val typeDeclaration = paramType.declaration as? KSClassDeclaration
    if (typeDeclaration != null && typeDeclaration.modifiers.contains(Modifier.DATA)) {
        val hasCompanionObject = typeDeclaration.declarations
            .filterIsInstance<KSClassDeclaration>()
            .any { it.isCompanionObject }

        if (hasCompanionObject) {
            // This data class has a companion object, so it can potentially have a field() function
            // Try to use it recursively
            logger.info("Found data class with companion object: $qualifiedTypeName, will try to use field() function")
            return "$typeName.field(label = \"$paramName\", initialValue = initialValue.$paramName)"
        }
    }

    // 3. Map primitive types to field types
    val fieldType = when (qualifiedTypeName) {
        "kotlin.String" -> {
            imports.add("me.tbsten.compose.preview.lab.field.StringField")
            "StringField"
        }
        "kotlin.Int" -> {
            imports.add("me.tbsten.compose.preview.lab.field.IntField")
            "IntField"
        }
        "kotlin.Long" -> {
            imports.add("me.tbsten.compose.preview.lab.field.LongField")
            "LongField"
        }
        "kotlin.Float" -> {
            imports.add("me.tbsten.compose.preview.lab.field.FloatField")
            "FloatField"
        }
        "kotlin.Double" -> {
            imports.add("me.tbsten.compose.preview.lab.field.DoubleField")
            "DoubleField"
        }
        "kotlin.Boolean" -> {
            imports.add("me.tbsten.compose.preview.lab.field.BooleanField")
            "BooleanField"
        }
        "kotlin.Byte" -> {
            imports.add("me.tbsten.compose.preview.lab.field.ByteField")
            "ByteField"
        }
        else -> {
            // 4. Fallback: check if there's a known field type for this type
            val potentialFieldType = findKnownFieldType(qualifiedTypeName, paramName, imports)
            if (potentialFieldType != null) {
                return potentialFieldType
            }

            logger.warn("Unsupported type $qualifiedTypeName for property $paramName. Falling back to StringField.")
            imports.add("me.tbsten.compose.preview.lab.field.StringField")
            "StringField"
        }
    }

    return "$fieldType(label = \"$paramName\", initialValue = initialValue.$paramName)"
}

/**
 * Searches for known field types in the compose-preview-lab library
 */
private fun findKnownFieldType(
    qualifiedTypeName: String?,
    paramName: String,
    imports: MutableSet<String>,
): String? {
    if (qualifiedTypeName == null) return null

    // Map known types to their corresponding field types
    return when (qualifiedTypeName) {
        "androidx.compose.ui.unit.Dp" -> {
            imports.add("me.tbsten.compose.preview.lab.field.DpField")
            "DpField(label = \"$paramName\", initialValue = initialValue.$paramName)"
        }
        "androidx.compose.ui.unit.DpOffset" -> {
            imports.add("me.tbsten.compose.preview.lab.field.DpOffsetField")
            "DpOffsetField(label = \"$paramName\", initialValue = initialValue.$paramName)"
        }
        "androidx.compose.ui.unit.DpSize" -> {
            imports.add("me.tbsten.compose.preview.lab.field.DpSizeField")
            "DpSizeField(label = \"$paramName\", initialValue = initialValue.$paramName)"
        }
        "androidx.compose.ui.unit.TextUnit" -> {
            imports.add("me.tbsten.compose.preview.lab.field.SpField")
            "SpField(label = \"$paramName\", initialValue = initialValue.$paramName)"
        }
        "androidx.compose.ui.graphics.Color" -> {
            imports.add("me.tbsten.compose.preview.lab.field.ColorField")
            "ColorField(label = \"$paramName\", initialValue = initialValue.$paramName)"
        }
        "androidx.compose.ui.Modifier" -> {
            imports.add("me.tbsten.compose.preview.lab.field.ModifierField")
            "ModifierField(label = \"$paramName\", initialValue = initialValue.$paramName)"
        }
        else -> null
    }
}
